JSONAnalysis
============

Methods for JSON analysis of response by the search engine APIs of Google custom search API, Yahoo! Boss API, Bing Search through Azure, Yahoo Content Analysis for extraction of semantic entities and categories, Diffbot Article API for extraction of semantic tags recognized, Youtube API v3.

Class that includes several functions that perform JSON parsing using JSON simple parser
 * The functions perform the following operations:
 * 
 * 1)GoogleJSONParsing: 
 *   Input: receives the JSON response by Google Custom search API in a string
 *   Output: extracts the links corresponding to the results on the JSON
 * 2)YahooJSONParsing
 *   Input: receives the JSON response by Yahoo! Boss API in a string
 *   Output: extracts the links corresponding to the results on the JSON
 * 3)BingAzureJSONParsing
 *   Input: receives the JSON response by Bing search API in Azure in a string
 *   Output: extracts the links corresponding to the results on the JSON
 * 4)SindiceTripleParse
 *   Input: receives the JSON response by Sindice Live API v2
 *   Output: the number of semantic triples recognized in a url
 *            the namespaces that were used in the url
 * 5)DiffbotParsing
 *   Input: receives the JSON response by Diffbot Article API
 *   Output: the (semantic) tags included in the JSON response
 * 6)YahooEntityJSONParsing
 *   Input: receives the JSON response by Yahoo Content Analysis API
 *          It also receives the query for which the corresponding urls to the search engine results
 *          were provided as input to Yahoo Content Analysis API
 *   Output: the semantic entities and categories recognized
 *           the number of semantic entities and categories which include the query as one of their terms
 * 7)GetYoutubeDetails
 *   Input: receives the JSON response by YouTube Data API v3 in a string
 *   Output: the textual details (title, description) regarding the video searched
 * 8)removeChars
 *   Input: receives text 
 *   Output: removes useless characters from the text
