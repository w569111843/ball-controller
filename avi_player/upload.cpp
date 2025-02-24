#include "upload.h"

extern WebServer server;
File fsUploadFile;

void getSDCardSpace() {
  String message = "";

  // 获取 SD 卡的总空间和已使用空间
  unsigned long usedBytes = SD_MMC.usedBytes();
  message = String(usedBytes);

  server.send(200, "text/plain", message);
}

//列出上传的文件
String listUploadDir() {
  String filename = "";
  String filename2 = "";
  File root = SD_MMC.open("/video", FILE_READ);
  String message = "";
  if (!root) {
    message = "{\"error\": \"Failed to open directory\"}";
    return message;
  }
  if (!root.isDirectory()) {
    message = "{\"error\": \"Not a directory\"}";
    return message;
  }
  File file = root.openNextFile();
  DynamicJsonDocument doc(1024);
  JsonArray array = doc.to<JsonArray>();

  while (file) {
    if (!file.isDirectory()) {
      String filename = String(file.path());
      String filename2 = String(file.name());
      unsigned long fileSize = file.size();
      unsigned long creationTime = file.getLastWrite();  // Some systems may support this, not all


      // Create a JSON object for each file
      JsonObject fileObject = array.createNestedObject();
      fileObject["name"] = filename2;
      fileObject["size"] = fileSize;
      fileObject["creationTime"] = creationTime;  // Time is in seconds since epoch (UNIX timestamp)
    }
    file = root.openNextFile();
  }

  // Convert the JSON document to a string
  serializeJson(doc, message);

  return message;
}

void listUploadFile() {
  String message = listUploadDir();
  server.send(200, "application/json", message);
}


void handleFileUpload() {
  HTTPUpload &upload = server.upload();
  if (upload.status == UPLOAD_FILE_START) {  // 如果上传状态为UPLOAD_FILE_START

    String filename = upload.filename;  // 建立字符串变量用于存放上传文件名
    filename = "/video/" + filename;    // 为上传文件名前加上"/"
    if (SD_MMC.exists(filename)) {
      server.send(400, "text/plain", "文件已经存在");  // 返回400错误，表示文件已存在
      return;  // 终止上传操作
    }

   
    // Serial.println("File Name: " + filename);                 // 通过串口监视器输出上传文件的名称

    fsUploadFile = SD_MMC.open(filename, FILE_WRITE);  // 在SD卡中建立文件用于写入用户上传的文件数据

  } else if (upload.status == UPLOAD_FILE_WRITE) {  // 如果上传状态为UPLOAD_FILE_WRITE

    if (fsUploadFile)
      fsUploadFile.write(upload.buf, upload.currentSize);  // 向SD卡文件写入浏览器发来的文件数据

  } else if (upload.status == UPLOAD_FILE_END) {                    // 如果上传状态为UPLOAD_FILE_END
    if (fsUploadFile) {                                             // 如果文件成功建立
      fsUploadFile.close();     
      server.send(200, "text/plain", "文件上传成功");                                    // 将文件关闭
    } else {                                                        // 如果文件未能成功建立
                                                                    //   Serial.println("File upload failed");               // 通过串口监视器输出报错信息
      server.send(500, "text/plain", "500: couldn't create file");  // 向浏览器发送相应代码500（服务器错误）
    }
  }
}

void deleteUploadFile() {
  String fileName = server.arg("fileName");
  String deletePath = "/video/" + fileName;
  char flag = 0;
  flag = deleteFile(SD_MMC, (char *)deletePath.c_str());
  if (flag) {
    server.send(200, "text/plain", "删除成功");
  } else {
    server.send(200, "text/plain", "删除失败");
    // Serial.println("Delete failed");
  }
}