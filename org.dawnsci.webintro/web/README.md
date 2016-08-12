# DAWN WebIntro Page #

This folder contains the web page which is loaded by the JavaFX WebView. 

## CSS ##
This folder contains compiled CSS files, it should never need to be edited (see SCSS below)

## Fonts ## 
This folder contains the fonts and icons used in the webintro pages. They are included locally so that DAWN can run without an internet connection.

The icon library being used is the google material design icon font: https://design.google.com/icons/

## img ##
Images which are used by the web page - e.g. background, logo

## JS ##
The JavaScript files used in the page. There are a number of open source libraries, along with an init.js file. This init.js file contains all of the application logic for the webintro page.

## SCSS ##
The web page makes use of SCSS, which needs to be compiled into CSS for the browser to interpret. In order to do this you need a tool called "Compass". http://compass-style.org/
Compass is written in Ruby, but I have compiled it into a JAR file using JRuby so that it is easy to use. The JAR file can be found in S:/science/DASC/scisoft/DAWN/tools/org.dawnsci.webintro/jcompass.jar. Example usage:

    cd {{PATH_TO_THIS_FOLDER}}
    java -jar {{PATH_TO_JCOMPASS}}/jcompass.jar -S compass compile --sass-dir scss/ --css-dir css/
    
or for development you can let Compass watch the css folder for any changes, and automatically update the css:

    cd {{PATH_TO_THIS_FOLDER}}
    java -jar {{PATH_TO_JCOMPASS}}/jcompass.jar -S compass watch --sass-dir scss/ --css-dir css/
    
SCSS does not change any of the standard features of CSS, so you can write normal CSS alongside SCSS. Any file beginning with an underscore is not compiled into its own CSS file. It is likely that the files beginning with an underscore are imported by another file (e.g. _materialize.scss is imported by style.scss). You should only need to edit style.scss to make changes to the look and feel of the webintro.
   	
## index.html ##
The HTML file which is opened by the browser. It also contains all of the handlebars templates in the header.
