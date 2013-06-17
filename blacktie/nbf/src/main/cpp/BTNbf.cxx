/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and others contributors as indicated
 * by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
#include "btnbf.h"
#include "xatmi.h"
#include "log4cxx/logger.h"
#include "NBFParser.h"

static log4cxx::LoggerPtr logger(log4cxx::Logger::getLogger("BTNbf"));

int find_string(char* buf, const char* str, int index) {
	int i;
	int found = 0;
	char* p = buf;
	char* q = buf;

	if(buf == NULL || str == NULL) {
		return -1;
	}
	int n = strlen(str);

	for(i = 0; i <= index; i++) {
		p = strstr(q, str);
		if(p == NULL) {
			found = 0;
			break;
		} else {
			found = 1;
		}
		q = p + n;
	}

	if(found) {
		return p - buf;
	}

	return -1;
}

int find_element_string(char* buf, char* attributeId, const char* value, int attributeIndex, int isset) {
	if(buf == NULL || attributeId == NULL || (value == NULL && isset == 0)) {
		return -1;
	}
	int ele_size = strlen(attributeId) + 3;
	char* element = (char*) malloc (sizeof(char) * ele_size);
	memset(element, 0, ele_size);
	element[0] = '\0';
	strcpy(element, "<");
	strcat(element, attributeId);
	strcat(element, ">");

	//printf("element is %s\n", element);
	int pos = find_string(buf, element, attributeIndex);
	free(element);

	if(isset == 0 && strlen(value) > 0 && buf[pos + strlen(attributeId) + 3] == '/') {
		return -1;
	}
	return pos;
}

void del_string(char* buf, int pos, int len) {
	int i, j;
	int n = strlen(buf);

	if(pos <= 0 || len <= 0 || pos + len >= n) {
		LOG4CXX_WARN(logger, (char*) "pos is " << pos << ", len is " << len << ", n is " << n);
		return;
	}

	for(i = pos, j = pos + len; j <= n; i++, j++) {
		buf[i] = buf[j];
	}
	for(i = n - len; i < n; i++) {
		buf[i] = '\0';
	}
}

void insert_string(char** buf, const char* s, int pos) {
	char* p = *buf;
	int n = strlen(p);

	if(s != NULL && pos >= 0 && pos <= n) {
		char* q = p + pos;
		char* tmp = (char*) malloc (sizeof(char) * (n - pos + 1));
		memset(tmp, '\0', n-pos+1);
		strcpy(tmp, q);

		int size = strlen(s) + n + 1;
		p = tprealloc (p, sizeof(char) * size);
		p[pos] = '\0';
		strcat(p, s);
		strcat(p, tmp);
		*buf = p;
		free(tmp);
	}
}

int btaddattribute(char** buf, char* attributeId, char* attributeValue, int len) {
	LOG4CXX_TRACE(logger, (char*) "btaddattribute");
	int rc = -1;
	char* p = *buf;
	char* q = strrchr(p, '<');

	if(q == NULL) {
		LOG4CXX_WARN(logger, (char*) "buffer not validate");
	} else {
		int n = q - p;

		int tagsize = strlen(attributeId) * 2 + 5 + 1;
		char* tag = (char*) malloc (sizeof(char) * tagsize);
		sprintf(tag, "<%s></%s>", attributeId, attributeId);
		int startPos = n + strlen(attributeId) + 2;
		insert_string(buf, tag, n);
		free(tag);

		NBFParser nbf;
		NBFParserHandlers handler(attributeId, 0);
		bool result;

		result = nbf.parse(*buf, "btnbf", &handler);
		if(result) {
			rc = 0;
			const char* type = handler.getType();
			if(type == NULL) {
				LOG4CXX_WARN(logger, (char*) "can not find type of attribute " << attributeId);
				del_string(*buf, n, tagsize); 
				return -1;
			}

			LOG4CXX_DEBUG(logger, (char*) "type is " << type);
			char* value = NULL;
			if(strcmp(type, "string") == 0) {
				value = (char*) malloc (sizeof(char) * (len + 1));
				memset(value, 0 , len + 1);
				strncpy(value, attributeValue, len);
			} else if(strcmp(type, "long") == 0) {
				value = (char*) malloc (sizeof(char) * 64);
				memset(value, 0 , 64);
				sprintf(value, "%ld", *((long*)attributeValue));
			} else if(strstr(type, "_type") != NULL) {
				int pos = find_string(attributeValue, ".xsd\">", 0);
				if(pos > 0) {
					int size = strlen(attributeValue);
					value = (char*) malloc (sizeof(char) * size);
					memset(value, 0 , size);
					strncpy(value, attributeValue + pos + 6, size - pos - strlen(attributeId) - 9);
				}
			}

			insert_string(buf, value, startPos);
			if(value != NULL) {
				LOG4CXX_DEBUG(logger, (char*) "release value");
				free(value);
			}
		} else {
			LOG4CXX_WARN(logger, (char*) "can not add attribute " << attributeId);
			del_string(*buf, n, tagsize); 
		}
	}

	return rc;
}

int btgetattribute(char* buf, char* attributeId, int attributeIndex, char* attributeValue, int* len) {
	LOG4CXX_TRACE(logger, (char*) "btgetattribute");
	int rc = -1;
	bool result;
	NBFParser nbf;
	NBFParserHandlers handler(attributeId, attributeIndex);

	result = nbf.parse(buf, "btnbf", &handler);
	if(result) {
		LOG4CXX_DEBUG(logger, (char*) "find attributeId:" << attributeId
				<< " at " << attributeIndex);

		const char* value = handler.getValue();
		const char* type = handler.getType();

		LOG4CXX_DEBUG(logger, (char*) "type is " << type);
		if(value != NULL) {
			rc = 0;
			if(type == NULL || strcmp(type, "string") == 0) {
				strncpy(attributeValue, value, *len);
				*len = strlen(attributeValue);
			} else if(strcmp(type, "long") == 0) {
				if(find_element_string(buf, attributeId, value, attributeIndex, 0) > 0) {
					*((long*)attributeValue) = atol(value);
					*len = sizeof(long);
				} else {
					rc = -1;
				}
			} else if(strstr(type, "_type") != NULL) {
				char* buf = tpalloc((char*)"BT_NBF", attributeId, 0);
				if(buf != NULL) {
					int pos = find_string(buf, ".xsd\">", 0);
					if(pos > 0) {
						//printf("value = %s\n", value);
						insert_string(&buf, value, pos + 6);
						*((char**)attributeValue) = buf;
					} else {
						LOG4CXX_WARN(logger, "buffer format error: " << buf);
						rc = -1;
					}
				} else {
					LOG4CXX_WARN(logger, "can not tpalloc " << type);
					rc = -1;
				}
			} else {
				LOG4CXX_WARN(logger, "can not support type of " << type);
				rc = -1;
			}
		}
	}

	return rc;
}

int btsetattribute(char** buf, char* attributeId, int attributeIndex, char* attributeValue, int len) {
	LOG4CXX_TRACE(logger, (char*) "btsetattribute");
	int rc = -1;
	bool result;
	NBFParser nbf;
	NBFParserHandlers handler(attributeId, attributeIndex);
	int pos;

	result = nbf.parse(*buf, "btnbf", &handler);
	if(result) {
		LOG4CXX_DEBUG(logger, (char*) "find attributeId:" << attributeId
				<< " at " << attributeIndex);

		const char* value = handler.getValue();
		const char* type = handler.getType();

		pos = find_element_string(*buf, attributeId, value, attributeIndex, 1);
		//printf("value is %s, type is %s, pos is %d\n", value, type, pos);
		if(pos > 0 ) {
			pos += strlen(attributeId) + 2;
			if(value != NULL && (*buf)[pos + 2] != '/') {
				int size = strlen(value);
				del_string(*buf, pos, size);
			}

			char* value;
			if(strcmp(type, "string") == 0) {
				value = (char*) malloc (sizeof(char) * (len + 1));
				memset(value, 0, len + 1);
				strncpy(value, attributeValue, len);
			} else if(strcmp(type, "long") == 0) {
				value = (char*) malloc (sizeof(char) * 64);
				memset(value, 0, 64);
				sprintf(value, "%ld", *((long*)attributeValue));
			} else if(strstr(type, "_type") != NULL) {
				int pos_value = find_string(attributeValue, ".xsd\">", 0);
				if(pos_value > 0) {
					int size = strlen(attributeValue);
					value = (char*) malloc (sizeof(char) * size);
					memset(value, 0 , size);
					strncpy(value, attributeValue + pos_value + 6, size - pos_value - strlen(attributeId) - 9);
				}
			}

			insert_string(buf, value, pos);
			if(value != NULL) {
				free(value);
			}

			rc = 0;
		}
	}

	return rc;
}

int btdelattribute(char* buf, char* attributeId, int attributeIndex) {
	LOG4CXX_TRACE(logger, (char*) "btdelattribute");
	int rc = -1;
	bool result;
	NBFParser nbf;
	NBFParserHandlers handler(attributeId, attributeIndex);
	int pos;

	result = nbf.parse(buf, "btnbf", &handler);
	if(result) {
		LOG4CXX_DEBUG(logger, (char*) "find attributeId:" << attributeId
				<< " at " << attributeIndex);

		const char* value = handler.getValue();

		if(value != NULL) {
			pos = find_element_string(buf, attributeId, value, attributeIndex, 0);
			if(pos > 0 ) {
				pos += strlen(attributeId) + 2;
				int size = strlen(value);
				del_string(buf, pos, size);
				rc = 0;
			}
		}
	}

	return rc;	
}

int btgetoccurs(char* buf, char* attributeId) {
	LOG4CXX_TRACE(logger, (char*) "btgetoccurs");
	int rc = -1;
	bool result;
	NBFParser nbf;
	NBFParserHandlers handler(attributeId, -1);

	result = nbf.parse(buf, "btnbf", &handler);
	if(result) {
		LOG4CXX_DEBUG(logger, (char*) "find attr " << attributeId);
		rc = handler.getOccurs();
	} else {
		LOG4CXX_WARN(logger, (char*) "can not find attr " << attributeId);
	}

	return rc;
}
