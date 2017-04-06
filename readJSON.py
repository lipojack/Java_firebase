import json
from time import sleep
while True:
	with open('testfile.json', 'r') as readjson:
		sleep(2)
		data = json.load(readjson)
		print (data)