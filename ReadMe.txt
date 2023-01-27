Steps for execution:

1. open terminal and move to zookeeper/bin and execute following command (excluding parentheses):
    (./zkServer.sh start-foreground)

2. Open terminal and navigate to the directory containing pom.xml file.

3. Execute following command (excluding parentheses):
    (java -jar target/leader-election-0.0.1-SNAPSHOT.jar 1 localhost:2181)

4. Create multiple nodes by executing command of step 3. Every time change second last argument (Say 1, 2, and so on).

5. To kill a node use CTRL+C in that node's running terminal.

%%%%%%%%%%
Arshad  %%
Aditya  %%
Nalini  %%
%%%%%%%%%%