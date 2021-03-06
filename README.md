# Hub
Hub for preprocessing of questions for yodaQA
Hub is interling between YodaQA-client and yodaQA. It's purpose is to get question from YodaQA-client, preprocess it, send it to yodaQA (or to other service, if it decides so), get answer and send answer back to web interface.

##Installation Instructions
Quick instructions for setting up, building and running:

  * You need Java 1.8 and Gradle
  * We assume that you cloned Hub and are now in the directory that contains this README.
  * ``gradlew build`` to build
  * ``gradlew run -PexecArgs="[Port to run] [Address of YodaQA]"`` to run

####Example
YodaQA runs on ``http://localhost:4567/``. To run HUB on port 4568 (it must differ from yodaQA's port), run in it's root directory ``gradlew build`` and ``gradlew run -PexecArgs="4568 http://localhost:4567/"``. To connect YodaQA-client to HUB add ``?e=http://localhost:4568/`` to the end of url.

##Dialog API
Dialog API expands YodaQA's API https://github.com/brmson/yodaqa/blob/master/doc/REST-API.md. Hub gets request from
web client and sends it further to YodaQA with minor changes. The list of changes follows.

####Question asking
To start answering process, use POST request method to `/q` with *text* attribute set to question and *dialogID* attribute set to current dialog id. If there is no dialog id (for example you don't know it, because you haven't started any dialog yet), new dialog is created. Return is id of question and id of dialog.

####Question retrieving
To retrieve answers use GET request method in format `/q/<qid>/<did>`, where *qid* is question id and *did* is dialog id. Boath were returned during questioning. Answers is returned in JSON.

####Dialog retrieving
To retrieve dialog use GET request method in format `/q/<did>`, where *did* is dialog id. Answer is ids of question contained in dialog in JSON format.

####Past dialogs
To retrieve past dialogs use GET request method in format `/q/?dialogs`. Past dialogs are returned in JSON.

####Artificial concepts
Artificial concepts are concepts, selected by users and not generated by YodaQA. Informations about them are send in fields:
* numberOfConcepts - number of concepts generated in total
* fullLabel{i} - full label of selected concept of number {i} (replace '{i}' with number)
* pageID{i} - page id of selected concept of number {i} (replace '{i}' with number)

##Coreference resolution
Concepts of last n answers are used, when third person pronoun(he, she, it) is founded in question's text.
``MAX_QUESTIONS_TO_REMEMBER_CONCEPT`` constant is used as n. Default value is 5.

####Example
When the first question is "What book wrote J. R. R. Tolkien?", the generated concept is "J. R. R. Tolkien". The second
question "Where was he born?" contains "he", so concept from the first question will be used.

##Transformations
Hub can transform questions and answers also. We are using "age transform" currently.

####Example
Age transform changes question from "How old is someone?" to "When he was born?". We do it, because YodaQA has better
success with finding of birth date. Answers are transformed back by calculation difference between today's date and date in answer.
Transformed answers are showed to users.
