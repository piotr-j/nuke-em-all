Hi!

This project requires Firebase project to run.


for admin stuff firebase tools are needed

ive used docker with 'firebase-tools' image.

Use `firebase login:ci` to login from the container

Need to make sure that port `9005` of the container is mapped so the login works.

Keep track of the token it spits out i guess.




   docker run -i -p 9005:9005 --name fb-tools -v c/dev/workspace/java/nukethemall/firebase:. andreysenov/firebase-tools


    docker run -i --rm -p 9005:9005 -v c/dev/workspace/java/nukethemall/firebase/:. andreysenov/firebase-tools
    
    
    docker run -i --rm -p 9005:9005 -v c\dev\workspace\java\nukethemall\firebase:/home/node andreysenov/firebase-tools
    
    docker run -i --rm --name=firebase-tools -p 9005:9005 -v c\dev\workspace\java\nukethemall\firebase:/home/node andreysenov/firebase-tools
    
    docker run -i --rm --name firebase-tools -p 9005:9005 -v c:/DEV/workspace/java/nukethemall/firebase:/home/node/firebase andreysenov/firebase-tools


