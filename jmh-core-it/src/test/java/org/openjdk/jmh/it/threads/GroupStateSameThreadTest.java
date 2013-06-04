/**
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package org.openjdk.jmh.it.threads;

import org.junit.Assert;
import org.junit.Test;
import org.openjdk.jmh.Main;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.it.Fixtures;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Tests if harness executes setup, run, and tearDown in the same workers.
 *
 * @author Aleksey Shipilev (aleksey.shipilev@oracle.com)
 */

public class GroupStateSameThreadTest {

    @State(Scope.Group)
    public static class MyState {

        private Set<Thread> setupRunThread = Collections.synchronizedSet(new HashSet<Thread>());
        private Set<Thread> setupIterationThread = Collections.synchronizedSet(new HashSet<Thread>());
        private Set<Thread> setupInvocationThread = Collections.synchronizedSet(new HashSet<Thread>());
        private Set<Thread> teardownRunThread = Collections.synchronizedSet(new HashSet<Thread>());
        private Set<Thread> teardownIterationThread = Collections.synchronizedSet(new HashSet<Thread>());
        private Set<Thread> teardownInvocationThread = Collections.synchronizedSet(new HashSet<Thread>());
        private Set<Thread> testInvocationThread = Collections.synchronizedSet(new HashSet<Thread>());

        @Setup(Level.Trial)
        public void setupRun() {
            setupRunThread.add(Thread.currentThread());
        }

        @Setup(Level.Iteration)
        public void setupIteration() {
            setupIterationThread.add(Thread.currentThread());
        }

        @Setup(Level.Invocation)
        public void setupInvocation() {
            setupInvocationThread.add(Thread.currentThread());
        }

        @TearDown(Level.Trial)
        public void tearDownRun() {
            teardownRunThread.add(Thread.currentThread());
        }

        @TearDown(Level.Iteration)
        public void tearDownIteration() {
            teardownIterationThread.add(Thread.currentThread());
        }

        @TearDown(Level.Invocation)
        public void tearDownInvocation() {
            teardownInvocationThread.add(Thread.currentThread());
        }

        @TearDown(Level.Trial)
        public void teardownZZZ() { // should perform last
            Assert.assertFalse("Test sanity", testInvocationThread.isEmpty());
            Assert.assertTrue("test <: setupRun", testInvocationThread.containsAll(setupRunThread));
            Assert.assertTrue("test <: setupIteration", testInvocationThread.containsAll(setupIterationThread));
            Assert.assertTrue("test <: setupInvocation", testInvocationThread.containsAll(setupInvocationThread));
            Assert.assertTrue("test <: teardownRun", testInvocationThread.containsAll(teardownRunThread));
            Assert.assertTrue("test <: teardownIteration", testInvocationThread.containsAll(teardownIterationThread));
            Assert.assertTrue("test <: teardownInvocation", testInvocationThread.containsAll(teardownInvocationThread));
        }
    }

    @GenerateMicroBenchmark
    @BenchmarkMode(Mode.All)
    @Warmup(iterations = 0)
    @Measurement(iterations = 5, time = 100, timeUnit = TimeUnit.MILLISECONDS)
    @Threads(4)
    public void test(MyState state) {
        state.testInvocationThread.add(Thread.currentThread());
        Fixtures.work();
    }

    @Test
    public void invoke() {
        Main.testMain(Fixtures.getTestMask(this.getClass()) + " -foe");
    }

}
