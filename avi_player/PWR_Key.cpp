#include "PWR_Key.h"

static uint8_t BAT_State = 0; 
static uint8_t Device_State = 0; 
static uint16_t Long_Press = 0;
extern uint8_t LCD_Backlight;

void PWR_Loop(void)
{
  if(BAT_State){ 
    if(!digitalRead(PWR_KEY_Input_PIN)){   
      if(BAT_State == 2){         
        Long_Press ++;
        if(Long_Press >= Device_Sleep_Time){
          if(Long_Press >= Device_Sleep_Time && Long_Press < Device_Restart_Time)
            Device_State = 1;
          else if(Long_Press >= Device_Restart_Time && Long_Press < Device_Shutdown_Time)
            Device_State = 2;
          else if(Long_Press >= Device_Shutdown_Time)
            Shutdown(); 
        }
      }
    }
    else{
      if(BAT_State == 1)   
        BAT_State = 2;
      Long_Press = 0;
    }
  }
}
void Fall_Asleep(void)
{
  LCD_Backlight = 0;
  esp_sleep_enable_wifi_wakeup();
  // 进入 Light Sleep 模式
  esp_light_sleep_start();
}
void Restart(void)
{

}
void Shutdown(void)
{
  
}
void PWR_Init(void) {
  pinMode(PWR_KEY_Input_PIN, INPUT);    
  pinMode(PWR_Control_PIN, OUTPUT);
  digitalWrite(PWR_Control_PIN, LOW);
  vTaskDelay(100);
  if(!digitalRead(PWR_KEY_Input_PIN)) {   
    BAT_State = 1;               
    digitalWrite(PWR_Control_PIN, HIGH);
  }
}
