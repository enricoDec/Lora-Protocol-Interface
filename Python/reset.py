#!/usr/bin/python
import sys

import RPi.GPIO as GPIO
import time

print("RESET GPIO")
GPIO.setmode(GPIO.BCM)
GPIO.setup(18, GPIO.OUT)
GPIO.output(18, GPIO.LOW)
time.sleep(1)
GPIO.output(18, GPIO.HIGH)
GPIO.cleanup()
print("RESET GPIO FIN")
