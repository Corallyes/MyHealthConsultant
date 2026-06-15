@echo off
netsh advfirewall firewall add rule name="Python OCR" dir=in action=allow protocol=TCP localport=8000
echo Done! Port 8000 is now open.
pause
