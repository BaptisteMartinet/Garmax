package griffith.baptiste.martinet.garmax

class GPXParsingError(tag: GPXHelper.TagEnum, message: String) : Exception("GPX parsing error around ${tag.name}: $message")