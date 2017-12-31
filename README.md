# taskflow

taskflow is a task manager that lets you create flowing tasks. it is fully object oriented and
can make working with tasks pretty easy.

with taskflow you can:

- create task flows made of tasks waiting each other
- link those task flows remotely by naming them for making those task flows
    wait each other
- keep linked task flows forever or destroy them whenever you want
- cancel flowing tasks according to results of tasks inside them
- suspend task flows as long as you want
- reuse the task flows or tasks you created as much as you want with different inputs
- create your own tasks and task flows to use them with already existing ones
- create repeating tasks which can repeat endlessly with specified period

taskflow does not contain:

- `static` but not `private` variables
- getters and setters
- `static` methods
- `null` references
- `extends` keyword on anything other than interfaces
- mutable objects
- any object without a purpose to live (data objects for example)
- any method that has more than one purpose
- any not `final` but `public` method without `@Override` annotation

## how can i use this

let's look at these sample codes:

```java
final TaskFlow<String> flow = new BasicTaskFlow<>(
    "unimportant for a non linked flow", myPlugin
);

final Task<String> task = new FlowingSyncTask<>(theString -> {
    System.out.println(theString);
    return theString + " another string";
}, new RepeatingAsyncTask<>(2, 5, theString -> {
    System.out.println(theString + "\nthis task runs after the previous one");
    return "switched to sync task to finish it off";
}, new SyncTask<>(theString -> {
    System.out.println(theString);
})));

flow.start("firstInput", task);
flow.start("secondInput", task);
```

output of the above code:

```terminaloutput
[05:10:23 INFO]: firstInput
[05:10:23 INFO]: firstInput another string
this task runs after the previous one
[05:10:23 INFO]: firstInput another string
this task runs after the previous one
[05:10:23 INFO]: switched to sync task to finish it off
[05:10:23 INFO]: secondInput
[05:10:23 INFO]: secondInput another string
this task runs after the previous one
[05:10:23 INFO]: secondInput another string
this task runs after the previous one
[05:10:24 INFO]: switched to sync task to finish it off
```

```java
// at some part of the project
final TaskFlow<String> linkedFlow = new LinkedTaskFlow<>("identifier", myPlugin);

linkedFlow.start("sample text", new AsyncTask<>(s -> {
    System.out.println(s);
}));

// at some other part of the project
final TaskFlow<Integer> sameLinkedFlow = new LinkedTaskFlow<>("identifier", myPlugin);

sameLinkedFlow.start(31, new WaitingTask<>(35,
    new AsyncTask<>(thirtyone -> {
        System.out.println("these flows will wait each other and start in order");
    })
));
```

output of the above code:

```terminaloutput
[05:26:36 INFO]: sample text
[05:26:38 INFO]: these flows will wait each other and start in order
```

but to get this beauty you have to shadow/shade (extract it to your jar file) taskflow
into your plugin. for gradle you can begin with this build.gradle example:

```groovy
buildscript {
    repositories {
        jcenter()
    }
    dependencies {
        classpath 'com.github.jengelman.gradle.plugins:shadow:latestVersionNumber'
    }
}

apply plugin: 'com.github.johnrengelman.shadow'
apply plugin: 'java'

group = 'me.blackness.test'
version = '0.0.0'

sourceCompatibility = '1.8'
targetCompatibility = '1.8'

repositories {
    maven {
        url 'https://hub.spigotmc.org/nexus/content/repositories/snapshots/'
    }
    mavenCentral()
    mavenLocal()
}

compileJava {
    options.forkOptions.executable = 'javac'
    options.encoding = 'UTF-8'
}

dependencies {
    // compileOnly for not shading/shadowing
    compileOnly group: 'org.spigotmc', name: 'spigot-api', version: 'spigotVersion'
    compile files('/location/to/taskflow.jar')
}

shadowJar {
    baseName = 'jarfilename'
    classifier = null
}

configurations {
    testCompile.extendsFrom compileOnly
}

build.dependsOn shadowJar

```

and run gradle task named "build" to generate your jar file like this: `gradle build`

----------

please do feel free to ask any of your questions, share your ideas through issues tab above.

you can find me on [spigot](https://spigotmc.org/) as "Menfie" and on [discord](https://discordapp.com/)
as "Personinblack#6059"

----------

<sub><sub><sub><sup>stay black!
