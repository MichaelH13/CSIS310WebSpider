import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Class that crawls the tree of a given starting site and displays the sub page
 * count and external link count of that site.
 * 
 * @author X1825794
 *
 */
public class WebSpider
{
   public static void main(String[] args)
   {
      final String ERROR_INVALID_LINK = "Error: A connection error occured.";
      final String ERROR_MIN_ARGS = "You must provide at least one argument.";
      final int MIN_ARGS = 1;
      HashMap<String, AtomicInteger> subPages = new HashMap<>();
      HashSet<String> externalLinks = new HashSet<>();
      Deque<String> linksToVisitStack = new ArrayDeque<>();

      // Verify the length of the arguments.
      if (args.length < MIN_ARGS)
      {
         System.err.println(ERROR_MIN_ARGS);
      }
      else
      {
         // Test the initial URL to verify that it is valid
         try
         {
            new HTMLLinks(args[0]);
         }
         catch (IOException e)
         {
            System.err.println(ERROR_INVALID_LINK);
         }

         // Add our initial site to our links to visit stack
         linksToVisitStack.push(args[0]);
         subPages.put(args[0], new AtomicInteger(1));

         // While we still have more links to visit, pop a link from the
         // links to visit stack and visit it and update our count of
         // occurrences of it.
         while (!linksToVisitStack.isEmpty())
         {
            try
            {
               // Iterate over all of the links contained on the current page.
               // If we have already seed the child link, then increment the
               // count
               // of the child link. Otherwise add the child link to the
               // hashmap list of links and set the count to 1.
               for (String link : new HTMLLinks(linksToVisitStack.pop()))
               {
                  // If we have not seen the current link yet, then push it to
                  // the links to visit stack.
                  if (!(subPages.containsKey(link) || linksToVisitStack
                           .contains(link)) && link.contains(args[0]))
                  {
                     // Add the current link to the links to visit stack.
                     linksToVisitStack.push(link);
                     subPages.put(link, new AtomicInteger(1));
                  }
                  // If we have seen this link, then just increment the count
                  // for it in our subpage hashmap.
                  else if (subPages.containsKey(link) && link.contains(args[0]))
                  {
                     // Get the count for the current link and increment it by
                     // 1.
                     subPages.get(link).getAndIncrement();
                  }
                  // If it is not one of the subpages, then it is an external
                  // link so we will add it to our external links set.
                  else
                  {
                     externalLinks.add(link);
                  }
                  System.out.println(link);
               }
            }
            catch (IOException e)
            {
               System.err.println(ERROR_INVALID_LINK);
            }
         }

         // Display the results.
         System.out.println(subPages.size() + " web pages");
         System.out
                  .println(externalLinks.size() + " external pages referenced");
      }

      System.exit(1);
   }
}