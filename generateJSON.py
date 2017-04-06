import json
import random

from time import sleep
for k in range(5):

	for i in range(100):
		sleep(0.1)
		with open('gesture.json','r') as readjson:
		    data = json.load(readjson)
		    #data = {"type":"gesture", "value":i}
		    data["value"] = i/50+1
		    #sleep(5)
		    print (data)
		    readjson.close()
		    with open('gesture.json', 'w') as writejson:
		    	json.dump(data, writejson)

	for i in range(20):
		sleep(0.1)
		with open('gaze.json','r') as readjson:
		    data = json.load(readjson)
		    #data = {"type":"gaze\e", "value":i}
		    data["value"] = 1
		    #sleep(5)
		    print (data)
		    readjson.close()
		    with open('gaze.json', 'w') as writejson:
		    	json.dump(data, writejson)

	for i in range(20):
		sleep(0.1)
		with open('gaze.json','r') as readjson:
		    data = json.load(readjson)
		    #data = {"type":"gaze", "value":i}
		    data["value"] = 0
		    #sleep(5)
		    print (data)
		    readjson.close()
		    with open('gaze.json', 'w') as writejson:
		    	json.dump(data, writejson)
