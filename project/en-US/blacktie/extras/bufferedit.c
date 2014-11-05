// add a attribute in buffer
char name[16];
char value[16];
int len = 16;

char* buf = tpalloc((char*) "BT_NBF", (char*) "employee", 0);
strcpy(name, "test");

rc = btaddattribute(&buf, (char*) "name", name, strlen(name));
rc = btgetattribute(buf, (char*) "name", 0, (char*) value, &len);

strcmp(value, "test");
