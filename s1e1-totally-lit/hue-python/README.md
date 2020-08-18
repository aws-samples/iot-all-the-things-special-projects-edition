## Hue Dance Party in Python

This directory is the Python3 version of the Hue Dance party script shown in the episode.

### How to discover your bridge IP, Hue lightbulb names and groups

To run the danceparty.py script, you will need to know your Hue bridge IP address and the names of either the Hue lights you want to control 
or the groups they are a part of.

In order to get this information, run the script:

`./lightbulb_discovery.py HUE_USERNAME`

The output will look something like this:

```
Your bridge IP is: 192.168.1.169
Light 1 is named Hue white lamp 1
Light 2 is named Hue white lamp 2
Light 3 is named Hue color Lamp 1

For Group 1:
The name of the group is: The Dining Room
The lights are: ['Hue white lamp 1']
```

### Running the dance party

When you want to turn on the dance party, you can either choose groups or lights.

For selected lights, run:

` ./danceparty.py 192.168.1.10 USERNAME 'Hue color Lamp 1' 'Hue color Lamp 2'`

For a group or groups:

`./danceparty.py 192.168.1.10 USERNAME --groups 'The Living Room' 'The Dining Room'`
