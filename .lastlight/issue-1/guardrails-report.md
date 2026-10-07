# Guardrails Report — issue #1

## Ecosystem

Maven / Java 17 (Spring Boot 3.4.2, WebFlux). Java 21 JDK available on host; Maven 3.9.x available.

## Dependency Install

`mvn test-compile` completed successfully (EXIT=0). All Maven dependencies resolved and downloaded.

## Checks

### 1. Test Framework

**PRESENT — PASS.**

Test runner: JUnit Jupiter (JUnit 5) via `maven-surefire-plugin 3.0.0-M5`.

Test files:
- `src/test/java/com/mahmoud/firstChallenge/ProductServiceTest.java`
- `src/test/java/com/mahmoud/firstChallenge/ProductControllerTest.java`
- `src/test/java/com/mahmoud/firstChallenge/FirstChallengeApplicationTests.java`

Full test command (written to gate script):
```
mvn test -B
```

Test compilation succeeded (`mvn test-compile`, EXIT=0).

### 2. Linting

**NOT CONFIGURED.** No Checkstyle, SpotBugs, PMD, or other lint plugin found in `pom.xml`. Not a blocker.

### 3. Type Checking

**IMPLICIT — PASS.** Java compilation (`mvn test-compile`, EXIT=0) IS the type check for this ecosystem. No separate tsconfig/mypy/cargo check needed.

### 4. CI Pipeline

**ABSENT.** No `.github/workflows/` directory found. Not a blocker.

---

## Gate Script

```sh
#!/usr/bin/env bash
set -euo pipefail
mvn test -B
```

---

*Harness will append suite results below.*

## Full test suite gate (run by the harness)

- Verdict: READY — full test suite passed (exit 0) in 67s
- Command (`.git/lastlight-gate.sh`):
```sh
#!/usr/bin/env bash
set -euo pipefail
mvn test -B
```
- Exit code: 0 · duration: 67s · limit: gate.timeoutSeconds=900s

Last 60 lines of output:
```
	at com.mongodb.internal.Locks.withLock(Locks.java:56) ~[mongodb-driver-core-5.2.1.jar:na]
	at com.mongodb.internal.Locks.withLock(Locks.java:34) ~[mongodb-driver-core-5.2.1.jar:na]
	at com.mongodb.internal.connection.netty.NettyStream$OpenChannelFutureListener.operationComplete(NettyStream.java:521) ~[mongodb-driver-core-5.2.1.jar:na]
	at com.mongodb.internal.connection.netty.NettyStream$OpenChannelFutureListener.operationComplete(NettyStream.java:504) ~[mongodb-driver-core-5.2.1.jar:na]
	at io.netty.util.concurrent.DefaultPromise.notifyListener0(DefaultPromise.java:590) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.DefaultPromise.notifyListeners0(DefaultPromise.java:583) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.DefaultPromise.notifyListenersNow(DefaultPromise.java:559) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.DefaultPromise.notifyListeners(DefaultPromise.java:492) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.DefaultPromise.setValue0(DefaultPromise.java:636) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.DefaultPromise.setFailure0(DefaultPromise.java:629) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.DefaultPromise.tryFailure(DefaultPromise.java:118) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.fulfillConnectPromise(AbstractNioChannel.java:326) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:342) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]
Caused by: io.netty.channel.AbstractChannel$AnnotatedConnectException: Connection refused: getsockopt: localhost/[0:0:0:0:0:0:0:1]:27017
Caused by: java.net.ConnectException: Connection refused: getsockopt
	at java.base/sun.nio.ch.Net.pollConnect(Native Method) ~[na:na]
	at java.base/sun.nio.ch.Net.pollConnectNow(Net.java:682) ~[na:na]
	at java.base/sun.nio.ch.SocketChannelImpl.finishConnect(SocketChannelImpl.java:973) ~[na:na]
	at io.netty.channel.socket.nio.NioSocketChannel.doFinishConnect(NioSocketChannel.java:336) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.AbstractNioChannel$AbstractNioUnsafe.finishConnect(AbstractNioChannel.java:339) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKey(NioEventLoop.java:776) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeysOptimized(NioEventLoop.java:724) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.processSelectedKeys(NioEventLoop.java:650) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.channel.nio.NioEventLoop.run(NioEventLoop.java:562) ~[netty-transport-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:997) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30) ~[netty-common-4.1.117.Final.jar:4.1.117.Final]
	at java.base/java.lang.Thread.run(Thread.java:1583) ~[na:na]

2026-10-07T12:07:48.564+01:00  INFO 27392 --- [First Challenge] [           main] c.m.f.FirstChallengeApplicationTests     : Started FirstChallengeApplicationTests in 10.851 seconds (process running for 13.173)
[ERROR] Java HotSpot(TM) 64-Bit Server VM warning: Sharing is only supported for boot loader classes because bootstrap classpath has been appended
Mockito is currently self-attaching to enable the inline-mock-maker. This will no longer work in future releases of the JDK. Please add Mockito as an agent to your build what is described in Mockito's documentation: https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html#0.3
WARNING: A Java agent has been loaded dynamically (C:\Users\mahmo\.m2\repository\net\bytebuddy\byte-buddy-agent\1.15.11\byte-buddy-agent-1.15.11.jar)
WARNING: If a serviceability tool is in use, please run with -XX:+EnableDynamicAgentLoading to hide this warning
WARNING: If a serviceability tool is not in use, please run with -Djdk.instrument.traceUsage for more information
WARNING: Dynamic loading of agents will be disallowed by default in a future release
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 13.777 s - in com.mahmoud.firstChallenge.FirstChallengeApplicationTests
[INFO] Running com.mahmoud.firstChallenge.ProductControllerTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 5.677 s - in com.mahmoud.firstChallenge.ProductControllerTest
[INFO] Running com.mahmoud.firstChallenge.ProductServiceTest
[INFO] Tests run: 2, Failures: 0, Errors: 0, Skipped: 0, Time elapsed: 0.61 s - in com.mahmoud.firstChallenge.ProductServiceTest
[INFO] 
[INFO] Results:
[INFO] 
[INFO] Tests run: 5, Failures: 0, Errors: 0, Skipped: 0
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  01:03 min
[INFO] Finished at: 2026-10-07T12:07:59+01:00
[INFO] ------------------------------------------------------------------------
```
