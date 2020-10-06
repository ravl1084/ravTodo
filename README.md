# RAVtodo

Yet another Todo.txt tool. Why? Because I have long relied on Todo.txt and need to be able to use it in a Bash-less, Python-less, Ruby-less environment.

## Main Goals

- Implement standard todo.txt format.
- Implement threshold dates.
- Remain compatible with SimpleTask for Android, especially for recurring tasks.
- Implement outline planning for large projects.
- Support a capture-and-process workflow as described by David Allen's Getting Things Done methodology.

## Installation

- Create a configuration file called `ravTodo.conf` and place it in the folder that is your HOME environment variable. Currently, only one config parameter is used:

```
todo.path=/path/to/the/folder/where/your/file/lives

```


## Dependencies
 - [JColor](https://github.com/dialex/JColor)
 - JUnit5 for some testing

## Available Commands

 - `ls [terms]`: With no parameters, lists all to-dos. With parameters, provides a filtered list of to-dos matching the terms provided.
 - `do <id>`: Marks to-do with the provided ID number as done.
 - `archive`: Moves all completed to-dos to the done.txt file.
 - `next`: Adds next actions from existing outlines.
 - `process`: Walks user through all items marked with *@inbox* and prompts to update context and project, as recommended by David Allen's GTD methodology.

## Acknowledgements

- Gina Trapani for the brilliant todo.txt and the [Todo.txt](http://todotxt.org/) community.
- Mark Janssen for the brilliant [SimpleTask for Android](https://github.com/mpcjanssen/simpletask-android/blob/master/app/src/main/assets/index.en.md).
- Samuel Snyder for the brilliant [Outline-Todo](https://github.com/samuelsnyder/outline-todo.txt).

## License

GNU General Public License
