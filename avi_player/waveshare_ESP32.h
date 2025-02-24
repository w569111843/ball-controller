#pragma once

// Display
#include <Arduino_GFX_Library.h>
#define GFX_BL 15
Arduino_DataBus *bus = new Arduino_ESP32QSPI(
    21 /* cs */, 40 /* sck */, 46 /* d0 */, 45 /* d1 */, 42 /* d2 */, 41 /* d3 */);
Arduino_ST77916 *gfx = new Arduino_ST77916(bus, -1 /* RST */, 0 /* rotation */, true /* IPS */, 360 /* width */, 360 /* height */);

// SD card
#define SD_SCK 14
#define SD_MOSI 17 // CMD
#define SD_MISO 16// D0
#define SD_D1 -1
#define SD_D2 -1
#define SD_CS -1   // D3

// I2S
#define I2S_MCLK -1
#define I2S_BCLK 48
#define I2S_LRCK 38
#define I2S_DOUT 47
#define I2S_DIN -1

#define I2S_DEFAULT_GAIN_LEVEL 0