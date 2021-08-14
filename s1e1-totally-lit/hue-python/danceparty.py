#!/usr/bin/env python3
# CHANGES that need to happen: make it so that a room or lightbulb name can be used instead of numbers, add rainbow and off phases, enter transitions
import random
import sys

import requests

# Disable SSL warnings since the Hue bridge will have a self-signed certificate
requests.packages.urllib3.disable_warnings()

if (len(sys.argv) < 3):
    print("You must specify the following parameters (in order):")
    print("  - The Hue bridge's IP address")
    print("  - The Hue bridge's username")
    print("  - The Hue bridge's light or group names")
    print("")
    print("Example: ./danceparty.py 192.168.1.10 USERNAME 'Hue color Lamp 1' 'Hue color Lamp 2'")
    print("Example: ./danceparty.py 192.168.1.10 USERNAME --groups 'The Living Room' 'The Dining Room'")
    print("")
    print("You must specify at least one light or group but may specify any number of them by adding more parameters")
    sys.exit(1)

LOCAL_HUE = sys.argv[1]
USER_NAME = sys.argv[2].lstrip()

# All of the remaining parameters are light IDs
if sys.argv[3] == "--groups":
  LIGHT_GROUP = True
  IDS = sys.argv[4:]
else:
  LIGHT_GROUP = False
  IDS = sys.argv[3:]


COLOR_MAX = 65535
BRIGHT_MAX = 254

# Get information for all rooms
def get_rooms(LOCAL_HUE, USER_NAME):
    groups_info = {}
    group_url = 'http://'+ LOCAL_HUE + '/api/' + USER_NAME + '/groups'
    groups = requests.get(group_url)
    groups_data = groups.json()
    for g in groups_data:
        lights = {}
        lights['lights'] = groups_data[g]['lights']
        group_name = groups_data[g]['name']
        groups_info[group_name] = lights
        groups_info[group_name]['id'] = g
    return(groups_info)

# Get lights
def get_lights(LOCAL_HUE, USER_NAME):
    lights_info = {}
    light_url = 'http://'+ LOCAL_HUE + '/api/' + USER_NAME + '/lights'
    lights = requests.get(light_url)
    lights_data = lights.json()
    for l in lights_data:
        id = {}
        id['id'] = l
        light_name = lights_data[l]['name']
        lights_info[light_name] = id 
    return(lights_info)

# Obtains the URL for a specific light ID
def get_light_url(id):
    return 'http://' + LOCAL_HUE + '/api/' + USER_NAME + '/lights/' + id


# Obtains the URL to set a specific light's state directly
def get_light_state_url(id):
    return get_light_url(id) + '/state'


def get_rando(value):
    return random.randrange(value)

def get_light_status(id):
    state = requests.get(get_light_url(id))
    return (state.json())


# Canned JSON to turn lights on and off
on = '{"on": true}'
off = '{"on": false}'


def turn_light_on(id):
    send_request_to_bridge(id, on)


def dance_party_mode(lights_included):
    # Get a random brightness
    bright_val = str(get_rando(BRIGHT_MAX))

    # Get a random color
    color_val = str(get_rando(COLOR_MAX))

    # Create the JSON used to set the state with our random values
    random_on = '{"transitiontime":0, "sat":254, "bri":' + bright_val + ',"hue":' + color_val + '}'

    # Turn each light on with the specified color and then turn it off
    for id in lights_included:
        send_request_to_bridge(id, random_on)

    return


def send_request_to_bridge(id, on):
    requests.put(get_light_state_url(id), data=on)


def main():
    # First, get all lights and groups of lights
    room_data = get_rooms(LOCAL_HUE, USER_NAME)
    light_data = get_lights(LOCAL_HUE, USER_NAME)
    # Loop through each light and force it to be on if necessary. No lights can be off during dance party mode!
    lights_included = []
    if LIGHT_GROUP:
        for room in IDS:
            lights_included.append(room_data[room]['lights'])
    else:
        for bulb in IDS:
            lights_included.append(light_data[bulb]['id'])
    for light in lights_included:
        light_stat = get_light_status(light)
        if light_stat['state']['on'] == False:
            turn_light_on(light)

    # Loop forever. Dance parties must not end!
    while True:
        dance_party_mode(lights_included)


if __name__ == "__main__":
    main()
