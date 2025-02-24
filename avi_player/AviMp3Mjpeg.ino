const char *root = "/sdcard";
#include <string>

#include "waveshare_ESP32.h"

#include <FFat.h>
#include <LittleFS.h>
#include <SPIFFS.h>
#include <SD_MMC.h>
#include <WebServer.h>
#include <WiFi.h>

#include "myServer.h"
uint16_t *output_buf;
#include "AviFunc.h"
#include "PWR_Key.h"
#include "BAT_Driver.h"

uint8_t LCD_Backlight = 50;
TaskHandle_t Task_Server;
WebServer server(80);
bool isPlaying = true;
String avi_filename = "/sdcard/video/test2.avi";
String previous_avi_filename = "/sdcard/video/test2.avi";
bool newVedio = true;
int GAIN_LEVEL = 0;
bool isMuted = false;

void handleClientTask(void *parameter) {
  while (true) {
    PWR_Loop();
    BAT_Get_Volts();
    server.handleClient();  // 处理客户端请求
    vTaskDelay(pdMS_TO_TICKS(10));
  }
  vTaskDelete(NULL);
}

void setup() {
  Serial.begin(115200);
  Set_Backlight();
  Serial.println("AviMp3Mjpeg");

#ifdef GFX_EXTRA_PRE_INIT
  GFX_EXTRA_PRE_INIT();
#endif

  // Init Display
  if (!gfx->begin()) {
    Serial.println("gfx->begin() failed!");
  }
  gfx->fillScreen(BLACK);

#ifdef GFX_BL
  pinMode(GFX_BL, OUTPUT);
  digitalWrite(GFX_BL, HIGH);
#endif

  gfx->setTextColor(WHITE, BLACK);
  gfx->setTextBound(60, 60, 240, 240);

#ifdef AUDIO_MUTE_PIN
  pinMode(AUDIO_MUTE_PIN, OUTPUT);
  digitalWrite(AUDIO_MUTE_PIN, HIGH);
#endif

  i2s_init();

  if (!SD_MMC.begin(root, true /* mode1bit */, false /* format_if_mount_failed */, SDMMC_FREQ_DEFAULT))
    SD_MMC.setPins(SD_SCK, SD_MOSI /* CMD */, SD_MISO /* D0 */, SD_D1, SD_D2, SD_CS /* D3 */);
  if (!SD_MMC.begin(root, true /* mode1bit */, false /* format_if_mount_failed */, SDMMC_FREQ_HIGHSPEED)) {
    Serial.println("ERROR: File system mount failed!");
  } else {
    output_buf = (uint16_t *)aligned_alloc(16, gfx->width() * gfx->height() * 2);
    if (!output_buf) {
      Serial.println("output_buf aligned_alloc failed!");
    }

    avi_init();
  }
  start_ap_server();
  PWR_Init();
  BAT_Init();
  xTaskCreatePinnedToCore(handleClientTask, "HandleClient", 20480, NULL, 1, &Task_Server, 1);
}

void video_play_start() {
  if (isPlaying && newVedio) {
    
    bool isOpen = avi_open(avi_filename.c_str());
    if (!isOpen) {
      Serial.println("AVI open failed");
      return;
    }
    previous_avi_filename = avi_filename;
    newVedio = false;
  }
  if (isPlaying) {
    Serial.println("AVI start");
    avi_start_ms = millis();
    avi_next_frame_ms = avi_start_ms + ((avi_curr_frame + 1) * 1000 / avi_fr);

    avi_feed_audio();

    Serial.println("Start play audio task");
    BaseType_t ret_val = mp3_player_task_start();
    if (ret_val != pdPASS) {
      Serial.printf("mp3_player_task_start failed: %d\n", ret_val);
    }

    while (avi_curr_frame < avi_total_frames && previous_avi_filename == avi_filename && isPlaying) {
      avi_feed_audio();
      avi_draw(0, 0);
    }
  }
  if (isPlaying) {
    avi_close();
    Serial.println("AVI end");
    newVedio = true;
  } else {
    mp3_player_task_remove();
  }
}

void loop() {
  video_play_start();
}