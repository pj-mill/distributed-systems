## Distributed Search Engine.
In a typical search problem as an input to our system, ahead of time, we have a large set of documents which can be books, academic articles, legal documents, or websites.
a user then provides us with a search query in real time to get the most relevant documents or links to their search.
Based on that search query, we want to identify which documents are more relevant and which are less relevant and present all those results to the user.
There are a number of ways we can achieve this, but in the end we have chose the TD-IDF (Term Frequency - Inverse Document Frequency) algorithm as explained below.

### Algorithms

#### Search Term Count
The simplest approach we can take is a simple word count of our search terms.
We can simply count how many times each search term from the query appears in each document, then documents where the search terms appear many times we will rank high in relevance.

So for example, for the search terms, very fast cars, if in the particular document, the word "very" appears 20 times the word "fast" appears 10 times and the word "cars" appears 30 times. Then the score is simply going to be 60. In the end that we can simply sort the documents by the score in a descending order and present that sorted list to the user,

This algorithm unfairly favors, larger documents with more words in them.
For example, if our search term is car, we may have a short article about racing cars, where the word car appears very often.
And in the same time, we may have another very long book about something completely irrelevant.
However, just because the book is long, the search term can still appear more times overall than in the short article.
So in that case, the long and irrelevant book will score higher than the clearly more relevant article about racing cars.

#### Search Term Count / Total Number of Words
This  gives us their relative term frequency in each document, instead of just the raw count.
If we applied that approach in this case, we clearly see that the word "car" appears every 10 words in the short article about cars.
Whereas in the irrelevant long document, the word car constitutes only about 0.01%, which means it appears only about every 8,000 words.

Let's take the fast car as an example of a search query, the user is clearly interested in documents about cars and in particular, fast cars. 
Humans can clearly understand that the most important term here is a car. However, unfortunately, computers are not as perceptive and based on our algorithm, our a document scores will be incorrectly, skewed towards documents that have the common and less important word "the" more frequently.

The reason for the problem is that our terms are equally weighted in the algorithm, but are in fact not equal in their importance for the search.


#### TD-IDF (Term Frequency - Inverse Document Frequency) algorithm
##### Term Frequency 
TF of a term or word is the number of times the term appears in a document compared to the total number of words in the document.

##### Inverse Document Frequency
IDF of a term reflects the proportion of documents in the corpus that contain the term. Words unique to a small percentage of documents (e.g., technical jargon terms) receive higher importance values than words common across all documents (e.g., a, the, and).

TD-IDF = TF * IDF



