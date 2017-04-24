# **Bitcoin Miner Simulator**

This is a BitcoinMiner simulator created using scala and Akka framework. The project aims at understanding and simulating a bitcoin miner by creating a distributed system. The application was tested on an intel i5 core.

## **Experiments**

### Size of Worker Unit

The size of the each worker unit that we have kept for our workers is 500,000 which is the number of sub problems that each worker handles. After experimenting with different unit sizes, it was found that for the above stated value the ratio for CPU time to real time was almost equivalent to around 4 (number of cores on the test machine). Also for local execution the optimal number of workers was 12, as the optimal number of workers that could be initiated in scala and Akka is three times the number of cores on that system. The application was also ran on multiple machines, with 8 actors running.


## **Results:**

We were able to find bitcoins or sudo-bitcoin i.e. with 8 leading zeros using just one macine with 4 cores.

