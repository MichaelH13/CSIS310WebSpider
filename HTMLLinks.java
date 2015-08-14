import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.text.MutableAttributeSet;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLEditorKit;

/**
 * Parses a web page and builds a collection of the links on that page Note: In
 * this class redirects are not followed.
 *
 * @author David M. Hansen with limited changes made by Joel Kelley on 12-11-08
 * @version 1.7
 */
public class HTMLLinks implements Iterable<String>
{

   /**
    * Retrieve the web page given by the URL and populate ourself with the links
    * from that page
    *
    * @param url
    *           to access and parse
    *
    * @throws MalformedURLException
    *            on a badly formed URL
    * @throws IOException
    *            on network error
    */
   public HTMLLinks(String url) throws MalformedURLException, IOException
   {
      HttpURLConnection connection;
      // Create a parser to parse the page. We use our own custom parser
      // given below
      HTMLParser kit = new HTMLParser();
      HTMLEditorKit.Parser webPageParser = kit.getParser();

      // The URL for this page is given by the parameter
      p_url = new URL(url);

      // Open a connection to the web page using the URL.
      // We're only going to deal with HTTP URLs (a URL could be a
      // "mailto" tag, for example). So we'll cast the connection to an
      // HttpURLConnection and, if that fails, ignore the response since
      // it wont have any links anyway
      try
      {
         connection = (HttpURLConnection) p_url.openConnection();
      }
      catch (ClassCastException e)
      {
         // No links to extract, so simply set a reasonable response
         // code and return
         p_responseCode = HttpURLConnection.HTTP_NO_CONTENT;
         return;
      }

      // Set the connection to not follow any redirects
      connection.setFollowRedirects(false);

      // Remember the response code we received from the server for this page
      p_responseCode = connection.getResponseCode();

      // Only process pages with HTML content
      if ((connection.getContentType() != null)
               && connection.getContentType().toLowerCase()
                        .contains("text/html") && p_responseCode < 400)
      {
         // The parser works by finding each "element" in the web page and
         // then calling a method to deal with those elements. So we create
         // a "callback" object that we give to the parser to call when it
         // finds elements. Invoke the parser on the open web page stream and
         // use the
         // "callback" object to deal with the elements as the parser detects
         // them.
         webPageParser.parse(
                  new InputStreamReader(connection.getInputStream()),
                  new HTMLParserCallback(), true);
      }
   }

   /**
    * @return an iterator over the links
    */
   public Iterator<String> iterator()
   {
      return p_links.iterator();
   }

   /**
    * @return the set of links
    */
   public Set<String> getLinks()
   {
      return p_links;
   }

   /**
    * @return the response(http-status) code of the website.
    */
   public int getResponseCode() // added by Joel
   {
      return p_responseCode;
   }

   /**
    * A subclass of the HTMLEditorKit that simply does what its parent does. We
    * only need this so we can create our own custom parser to avoid
    * character-set exceptions. RAF!
    */
   class HTMLParser extends HTMLEditorKit
   {
      public HTMLEditorKit.Parser getParser()
      {
         return super.getParser();
      }
   }

   /**
    * Class used by the parser to populate our list of links when they are
    * encountered
    */
   class HTMLParserCallback extends HTMLEditorKit.ParserCallback
   {
      /**
       * The only element we're interested in are "anchor" tags. Adds the href
       * text of any anchor tag to our list of links
       *
       * @param tag
       *           currently being evaluated (we only deal with * &lt;a&gt; tags
       * @param attributes
       *           is the set of attributes associated with this * tag
       * @param position
       *           of this tag
       */
      public void handleStartTag(HTML.Tag tag, MutableAttributeSet attributes,
               int position)
      {
         // The string representation of the HREF attribute of the tag
         // if this is an anchor tag
         URL link;
         Object hrefAttribute;

         // If this is an anchor tag, get the href (if it exists) and
         // add it to our list of links
         if (tag == HTML.Tag.A)
         {
            hrefAttribute = attributes.getAttribute(HTML.Attribute.HREF);
            // If the tag had an HREF attribute, then add it to our list
            // of links for this page
            if (hrefAttribute != null)
            {
               // Create a canonical URL using this link and the
               // current URL then store the URL as a string in the list
               // of links
               try
               {
                  link = new URL(p_url, hrefAttribute.toString());
                  p_links.add(link.toString());
               }
               // If the link is badly formed, we just ignore it
               catch (MalformedURLException m)
               {
               }
            }
         }
      }

      /**
       * Do nothing for end tags
       */
      public void handleEndTag(HTML.Tag tag, int position)
      {
      }

      /**
       * Do nothing for any other sort of tag
       */
      public void handleSimpleTag(HTML.Tag tag, MutableAttributeSet attributes,
               int position)
      {
      }

   }

   // The URL for the page holding these links
   private URL             p_url;

   // The collection of links we collect from a page
   private HashSet<String> p_links = new HashSet<String>();

   // The response code sent by the server when connection was mode.
   private int             p_responseCode;

   /**
    * Simple test program opens URL and prints out links
    *
    * @param args
    *           [0] URL to open
    *
    * @throws MalformedURLException
    *            on a badly formed URL
    * @throws IOException
    *            on network error
    */
   public static void main(String[] args) throws MalformedURLException,
            IOException
   {
      // Create an instance and iterate over the list of links, printing
      // them out and return the response(http-status) code of the parent site.

      HTMLLinks site = new HTMLLinks(args[0]);

      System.out.println(site.getResponseCode());

      for (String link : site)
      {
         System.out.println(link);
      }
   }
}
