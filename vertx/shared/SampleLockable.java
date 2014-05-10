/*
 * Copyright 2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.annotations.ReadLock;
import org.jboss.stm.annotations.State;
import org.jboss.stm.annotations.WriteLock;

@Transactional
public class SampleLockable implements Sample
{
    public SampleLockable (int init)
    {
        _isState = init;
    }
        
    @ReadLock
    public int value ()
    {
        return _isState;
    }

    @WriteLock
    public void increment ()
    {
        _isState++;
    }
        
    @WriteLock
    public void decrement ()
    {
        _isState--;
    }

    @State
    private int _isState;
}