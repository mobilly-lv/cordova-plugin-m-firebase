/*
Parser for config.xml file. Read plugin-specific preferences (from <m-firebase> tag) as JSON object.
original copied from cordova-universal-links-plugin
*/
var path = require('path');
var ConfigXmlHelper = require('./configXmlHelper.js');
var DEFAULT_SCHEME = 'http';

module.exports = {
  readPreferences: readPreferences
};

// region Public API

/**
 * Read plugin preferences from the config.xml file.
 *
 * @param {Object} cordovaContext - cordova context object
 * @return {Array} list of host objects
 */
function readPreferences(cordovaContext) {
  // read data from projects root config.xml file
  var configXml = new ConfigXmlHelper(cordovaContext).read();
  if (configXml == null) {
    console.warn('config.xml not found! Please, check that it exist\'s in your project\'s root directory.');
    return null;
  }

  // look for data from the <universal-links> tag
  var ulXmlPreferences = configXml.widget['m-firebase'];
  if (ulXmlPreferences == null || ulXmlPreferences.length == 0) {
    console.warn('<m-firebase> tag is not set in the config.xml. m-firebase plugin is not going to work.');
    return null;
  }

  var xmlPreferences = ulXmlPreferences[0];

  // read hosts
  var notificationIcons = constructIconsList(xmlPreferences);

  // read ios team ID
  var colors = getColorsXmlLocation(xmlPreferences);
  var strings = getStringsXmlLocation(xmlPreferences);
  var googleServicesJsonSrc = getGoogleServicesJsonSrc(xmlPreferences);
  var googleServicesPlistSrc = getGoogleServicesPlistSrc(xmlPreferences);

  return {
    'ic-notification': notificationIcons,
    'colorsXmlSrc': colors,
    'stringsXmlSrc': strings,
    'googleServicesJsonSrc': googleServicesJsonSrc,
    'googleServicesPlistSrc': googleServicesPlistSrc,
  };
}


function getGoogleServicesPlistSrc(xmlPreferences) {
  if (xmlPreferences.hasOwnProperty('google-services-plist')) {
    return xmlPreferences['google-services-plist'][0]['$']['src'];
  }
  return null;
}
function getGoogleServicesJsonSrc(xmlPreferences) {
  if (xmlPreferences.hasOwnProperty('google-services-json')) {
    return xmlPreferences['google-services-json'][0]['$']['src'];
  }
  return null;
}
function getStringsXmlLocation(xmlPreferences) {
  if (xmlPreferences.hasOwnProperty('strings')) {
    return xmlPreferences['strings'][0]['$']['src'];
  }
  return null;
}

function getColorsXmlLocation(xmlPreferences) {
  if (xmlPreferences.hasOwnProperty('colors')) {
    return xmlPreferences['colors'][0]['$']['src'];
  }
  return null;
}

function constructIconsList(xmlPreferences) {
  let iconsList = [];

  // look for defined hosts
  let xmlIconsList = xmlPreferences['ic-notification'];
  if (xmlIconsList == null || xmlIconsList.length == 0) {
    return [];
  }

  xmlIconsList.forEach(function(xmlElement) {
    let host = constructIconsEntry(xmlElement);
    if (host) {
      iconsList.push(host);
    }
  });

  return iconsList;
}


function constructIconsEntry(xmlElement) {
  let icons = {
      src: DEFAULT_SCHEME,
      size: '',
    };
  let iconsProperties = xmlElement['$'];
  if (iconsProperties == null || iconsProperties.length == 0) {
    return null;
  }

  if (iconsProperties['src'] != null) {
    icons.src = iconsProperties.src;
  }
  if (iconsProperties['size'] != null) {
    icons.size = iconsProperties.size;
  }
  return icons;
}
