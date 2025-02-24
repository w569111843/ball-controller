#include "myServer.h"

// 网页服务
String IPAD = "192.168.1.1"; // 在AP和STA模式下存储ESP32的IP地址
const char *ssid = "ESPS3-AP";
const char *password = "123456789";
char channel = 1;     // wifi信道
char ssid_hidden = 0; // WiFi隐身
extern bool isPlaying;
extern WebServer server;
extern String avi_filename;
extern uint8_t LCD_Backlight;
extern int GAIN_LEVEL;
extern bool isMuted;
#define GAIN_LEVEL_MAX 20

// 处理用户浏览器的HTTP访问
static void handleUserRequet()
{
  String path = server.uri(); // 获取用户请求网址信息
  Serial.println(path);
  String contentType = "text/plain";
  if (SD_MMC.exists(path))
  {                                           // 如果访问的文件可以在SD卡中找到
    File file = SD_MMC.open(path, FILE_READ); // 则尝试打开该文件
    server.streamFile(file, contentType);     // 并且将该文件返回给浏览器
    file.close();                             // 并且关闭文件
  }
  else
  {
    server.send(404, "text/plain", "File Not Found");
  }
}

void setVolumeLevel(){
  String levelStr = server.arg("level");
  int level = levelStr.toInt();
  if(level <= GAIN_LEVEL_MAX && level >= 0){
    GAIN_LEVEL = level;
  }
  server.send(200, "text/plain", "success");
}

void settingMute(){
  isMuted = !isMuted;
  server.send(200, "text/plain", "success");
}

void getVolumeSetting(){
  DynamicJsonDocument doc(100);
  JsonObject result = doc.to<JsonObject>();
  result["isMuted"] = isMuted;
  result["level"] = GAIN_LEVEL;
  String message = "";
  serializeJson(doc, message);
  server.send(200, "application/json", message);
}

void respondOK()
{
  server.send(200);
}

void videoPause()
{
  isPlaying = 0;
  server.send(200, "text/plain", "Video paused");
}

void videoPlay()
{
  isPlaying = 1;
  server.send(200, "text/plain", "Video play");
}

void settingFileName()
{
  String fileName = server.arg("fileName");
  fileName = "/sdcard/video/" + fileName;
  avi_filename = fileName;
  server.send(200, "text/plain", "success");
}

void get_soc(){
  String message = "";
  float voltage = BAT_Get_Volts();
  Serial.print("voltage");
  Serial.println(voltage);
  float soc = 0.0;
 if (voltage >= 4.25) {
        soc = 100.0; // 满电
    } else if (voltage >= 3.7) {
        // 3.7V - 4.25V 对应 50% - 100%
        soc = (voltage - 3.7) / (4.25 - 3.7) * 50.0 + 50.0;
    } else if (voltage >= 2.75) {
        // 2.75V - 3.7V 对应 0% - 50%
        soc = (voltage - 2.75) / (3.7 - 2.75) * 50.0;
    } else {
        soc = 0.0; // 电量耗尽
    }
    
  message = String(soc);
  server.send(200, "text/plain", message);
}

void Set_Backlight() {
  ledcAttach(LCD_Backlight_PIN, Frequency, Resolution);
  ledcWrite(LCD_Backlight_PIN, Dutyfactor);
  uint32_t Backlight = LCD_Backlight * 10;
  if (Backlight == 1000)
    Backlight = 1024;
  ledcWrite(LCD_Backlight_PIN, Backlight);
}

void sleepDevice(){
  isPlaying = 0;
  setCpuFrequencyMhz(80);
  LCD_Backlight = 0; 
  Set_Backlight();
  server.send(200, "text/plain", "success");
}

void stop_sleep(){
  setCpuFrequencyMhz(240);
  LCD_Backlight = 50;
  isPlaying = 1;
  Set_Backlight();
  server.send(200, "text/plain", "success");
}

void start_ap_server()
{
  IPAddress local_IP(192, 168, 1, 1); // 配置ESP32的IP地址
  IPAddress gateway(192, 168, 1, 1);  // 配置ESP32的网关
  IPAddress subnet(255, 255, 255, 0); // 配置ESP32的子网

  WiFi.softAPConfig(local_IP, gateway, subnet);

  WiFi.softAP(ssid, password);
  server.onNotFound(handleUserRequet);
  server.on("/sleep", HTTP_GET, sleepDevice);
  server.on("/stop-sleep", HTTP_GET, stop_sleep);
  server.on("/pause-video", HTTP_GET, videoPause);
  server.on("/play-video", HTTP_GET, videoPlay);
  server.on("/settingMute", HTTP_GET, settingMute);
  server.on("/setVolumeLevel", HTTP_GET, setVolumeLevel);
  server.on("/getVolumeSetting", HTTP_GET, getVolumeSetting);
  server.on("/getBatSoc", HTTP_GET, get_soc);
  server.on("/filelist", HTTP_GET, listUploadFile);
  server.on("/deleteUploadFile", HTTP_GET, deleteUploadFile);
  server.on("/getSDCardSpace", HTTP_GET, getSDCardSpace);
  server.on("/settingFileName", HTTP_GET, settingFileName);
  server.on("/upload",
            HTTP_POST,
            respondOK,
            handleFileUpload);
  server.begin();
}

void close_ap_server()
{
  server.close();
}