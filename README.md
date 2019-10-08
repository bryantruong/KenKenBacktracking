# comp560a1: KenKen Solver
### Bryan Truong, Shannon Goad

Clone this repository (stay on the master branch). There are **two** files that should be run: *SimpleSolution.java* and *BestSolution.java*.


Before doing so, you **must** change the absolute path of the input text file (which we have named `testCase.txt`), wherever you have chosen to clone this repository. 

In each of the aforementioned executable files, we read in the input text file by passing in the absolute path as a string to a helper function called "readFileInList". The line of code that needs to be modified is:

`List<String> input = readFileInList("/Users/bryantruong/git/comp560a1/src/simple/testCase.txt");`


After running each file, the result should be visible via stdout in the console, with the solved KenKen puzzle printed, followed by a blank line, then followed with the iteration count. 

Our writeup is here: https://drive.google.com/file/d/1ldoaexD8hJtl1yhB8XrZhMM_EON2yWlo/view?usp=sharing

Lastly, due to an unforeseen circumstance of a group member dropping last-minute, we have written a short addendum to explain the status and intended implementation of our Local Search, had the team member issue not arisen: https://docs.google.com/document/d/1RCwVEOjGgkt1N4xaL1IwTGRpUYugtPJyhiD7PC49WHs/edit?usp=sharing
