#include "Base64.h"

char* base64_encode(char* data, long* length) {
	long input = *length;
	long output = 0;

	char* toReturn = (char*)Base64::encode((const XMLByte*)data, 
			input, (XMLSize_t*)&output);
	*length = output;
	return toReturn;
}

char* base64_decode(char* data, long* length) {
	char* toReturn = (char*)Base64::decode((const XMLByte*)data, 
			(XMLSize_t*)length);
	return toReturn;
}

void base64_release(char** data) {
	XMLString::release(data);
}
