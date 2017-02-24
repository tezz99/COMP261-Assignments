List of files 
==============
All files are tab separated, and have one line describing each item.


nodeID-lat-lon.tab
==================
Nodes are intersections, or ends of roads, or points where two roads join.

The file has one line for each node, specifying the ID of
the node, and the latitude and longitude of the node.  

Note that latitude and longitude are specified in degrees, not distances. One
degree of latitude corresponds to 111.0 kilometers. One degree of longitude
varies, depending on the latitude, but it is reasonable to assume that for the
whole region of Auckland, one degree of longitude is 88.649 kilometers.  This
means that when you are computing distances between two points, you must scale
the latitude difference by 111.0 and scale the longitude difference by 88.649. 

-----------------------------------------------------------------------------
roadSeg-roadID-length-nodeID-nodeID-coords.tab
========================================
A road segment is a part of a road between two nodes. The only intersections on
a road segment are at its ends.  

The file has one line for each road segment, specifying the
ID of the road object this segment belongs to, the length of the segment and
the ID's of the nodes at each end of the segment, and the coordinates
of the points along the segment. The coordinates are a list of
latitude and longitude coordinates of the centerline of the road
segment. There will always be an even number of coordinates - pairs of
latitude and longitude. You will need to convert the latitude and
longitude to y and x coordinates.

-----------------------------------------------------------------------------
roadID-roadInfo.tab
===================
A road is a sequences of segments, with a name and other properties.  These
need not be an entire road - a real road that has different properties for some
parts will be represented in the data by several road objects, all with the
same name.  

The file has one line for each road. The columns are specified at the
top of the file. The first column is the ID of the road, and the 3rd
and 4th columns are the name of the road and the name of the city it
is in.  The meanings of the numeric colums are:

Type
------
  I can't find out what the road type means.


Speed limit categories:
-----------------------
  This is an important attribute for calculating good routes - a slightly
  longer route that has a faster speed limit is better than a shorter route
  with a slower speed limit
(Note, these are not accurate representations of the real Auckland speed
limits, but they are the best we have. You may use them as if they were
correct.) 

   0 = 5km/h    
   1 = 20km/h   
   2 = 40km/h   
   3 = 60km/h   
   4 = 80km/h   
   5 = 100km/h  
   6 = 110km/h  
   7 = no limit 


Road class:
-----------
 This is the other important attribute for calculating good routes - roads of a
 higher class are considered to be "faster" or at least "better".

   0 = Residential
   1 = Collector
   2 = Arterial 
   3 = Principal HW
   4 = Major HW 


One_way:
----------
        0 : both directions allowed
        1 : one way road, direction from beginning to end

notforcar, notforpedestrians, notforbicycle
-------------------------------------------
        0 : OK for this category of traffic
        1 : Not useable by this category of traffic

-----------------------------------------------------------------------------
restrictions.tab
================
A restriction prohibits traveling through one path of an intersection.

The file has one line for each restriction. Each line has five values:
nodeID-1, roadID-1, nodeID, roadID-2, nodeID-2.
  
The middle NodeID specifies the intersection involved.
The restriction specifies that it is not permitted to turn from the road
segment of roadID-1 going between nodeID-1 and the intersection into the
road segment of roadID-2 going between the intersection and nodeID-2  
-----------------------------------------------------------------------------
polygon-shapes.mp
=================
File with coordinates of polygons for drawing a nicer map - parks, coastline,
airport, rivers, etc
