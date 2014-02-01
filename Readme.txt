Read Me
Brendan Smith
100714420

There are some things that need to be known about my program.
Currently My program handles '?' values for all enumerated sets.  Numerical Values It cannot handle.

For Data Sets with numerical values in it and no '?' data pieces, it runs perfectly fine using Midpoint selection strategy.

The function that handles '?' Is called polymorphUknown
The probability set for random 1 variable or 2 variable branching is hard coded in, currently because getting accurate data
with it was too difficult.

If you want to re-enable it - Go to the DecideonCut() method in Node Class (Decision Tree File). There are a few commented out
lines there in the function that can be uncommented if needed.

The Program Generates A System.out Output as well as .Dot File that can be used to generate the Decision Tree.

Two change the sample and test size. Simply Change the fraction value in CreateSampleandTest()
The lower the fraction the more training data and less for testing.
