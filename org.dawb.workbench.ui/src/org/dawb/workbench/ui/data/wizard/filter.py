import numpy as np
'''

If applied to a dataset, whenever a 1D plot is made of it, 
either because it is 1D already or has been sliced to 1D - this filter 
will be called to determine the actual x and y values plotted.

The filter method may be called a lot therefore Dawn keeps a python
interpreter open for using with the filter.

The python interpreter used is the one defined for the project which
contains this script. If this is not a python pydev project, the first
interpreter in the list (if there is more than one) as defined in pydev
is used.

Filters a point for plotting.
@param x:  x an numpy array of the x values
@param y: an numpy array of the y values
@return: an array of numpy arrays, first in array is x data, second y data
'''

def filter1D(x, y):
    
    return [x, y]


'''
If we were an image filter, we would have:
@param image: 2d numpy array
@param xaxis: 1d numpy array of x-axis labels may be None
@param yaxis: 1d numpy array of y-axis labels may be None
@return: arrray containing modified elements
'''
def filter2D(image, xaxis, yaxis):
    
    return [image, xaxis, yaxis] 
    
