#!/usr/bin/env python3
import requests
import argparse

# Disable SSL warnings since the Hue bridge will have a self-signed certificate
requests.packages.urllib3.disable_warnings()

def get_bridge_ip():
    bridge_info = requests.get("https://discovery.meethue.com", verify=True)
    bridge_ip = bridge_info.json()[0]['internalipaddress']
    return(bridge_ip)

def print_lights(lights_info):
  for light in lights_info:
      print("Light {0} is named {1}".format(light, lights_info[light]['name']))

def get_lights(bridge_ip, username):
    light_url = 'https://'+ bridge_ip + '/api/' + username + '/lights'
    lights = requests.get(light_url, verify=False)
    lights_info = lights.json()
    print_lights(lights_info)
    return(lights_info)

def print_groups_and_lights(groups_info, lights):
    for k in groups_info:
        print("\nFor Group {0}: ".format(k))
        print("The name of the group is: {0}".format(groups_info[k]['name']))
        group_lights = []
        for light in groups_info[k]['lights']:
            group_lights.append(lights[light]['name'])
        print("The lights are: {0}".format(group_lights))

def get_rooms(bridge_ip, username):
    group_url = 'https://'+ bridge_ip + '/api/' + username + '/groups'
    groups = requests.get(group_url, verify=False)
    groups_info = groups.json()
    return(groups_info)

def main():
  bridge_ip = get_bridge_ip()
  print("Your bridge IP is: {}".format(bridge_ip))
  userid = args.username.lstrip()
  lights = get_lights(bridge_ip, userid)
  groups = get_rooms(bridge_ip, userid)
  print_groups_and_lights(groups, lights)

if __name__ == "__main__":
    parser = argparse.ArgumentParser(prog='lightbulb_discovery.py', description='This script will help you discover the IP address of your Hue bridge as well as the IDs of your light groups and individual lights.')
    parser.add_argument('username', help='The Hue developer username for your bridge. Go to https://developers.meethue.com/develop/get-started-2/#so-lets-get-started to learn more')

    args = parser.parse_args()
    main()

