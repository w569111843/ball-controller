#ifndef __UPLOAD_H
#define __UPLOAD_H

#include "common.h"
#include <WebServer.h>
#include <ArduinoJson.h>

void listUploadFile();
void handleFileUpload();
void deleteUploadFile();
void getSDCardSpace();

#endif