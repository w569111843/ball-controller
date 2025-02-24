#include <WebServer.h>
#include <WiFi.h>
#include "upload.h"
#include "common.h"
#include "BAT_Driver.h"
#include "PWR_KEY.h"

#define LCD_Backlight_PIN 5
#define Frequency 20000
#define Resolution 10
#define Dutyfactor 500
#define Backlight_MAX 100

void start_ap_server();
void close_ap_server();
void Set_Backlight();