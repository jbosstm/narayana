/*
 * SPDX short identifier: Apache-2.0
 */


package org.jboss.stm.internal.proxy;

import java.io.IOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

import org.jboss.stm.InvalidAnnotationException;
import org.jboss.stm.annotations.NotState;
import org.jboss.stm.annotations.RestoreState;
import org.jboss.stm.annotations.SaveState;
import org.jboss.stm.annotations.Transactional;
import org.jboss.stm.internal.RecoverableContainer;

import com.arjuna.ats.arjuna.ObjectModel;
import com.arjuna.ats.arjuna.common.Uid;
import com.arjuna.ats.arjuna.state.InputObjectState;
import com.arjuna.ats.arjuna.state.OutputObjectState;
import com.arjuna.ats.internal.arjuna.common.UidHelper;
import com.arjuna.ats.txoj.LockManager;

public class LockManagerProxy<T> extends LockManager
{
    public LockManagerProxy (T candidate)
    {
        super();
        
        _theObject = candidate;
        _container = null;
    }
    
    public LockManagerProxy (T candidate, RecoverableContainer<T> cont)
    {
        super(cont.objectType(), cont.objectModel());

        _theObject = candidate;
        _container = cont;
    }
    
    public LockManagerProxy (T candidate, int ot)
    {
        super(ot, ObjectModel.SINGLE);
        
        _theObject = candidate;
        _container = null;
    }
    
    public LockManagerProxy (T candidate, Uid u)
    {
        super(u, ObjectModel.SINGLE);
        
        _theObject = candidate;
        _container = null;
    }
    
    // if there's a Uid then this is a persistent object
    
    public LockManagerProxy (T candidate, Uid u, RecoverableContainer<T> cont)
    {
        super(u, cont.objectType(), cont.objectModel());  // TODO make configurable through annotation
        
        _theObject = candidate;
        _container = cont;
    }
    
    public boolean save_state (OutputObjectState os, int ot)
    {
        if (!super.save_state(os, ot))
            return false;   
        
        boolean res = false;
        
        try
        {
            /*
             * Priority is for @SaveState and @RestoreState first.
             */
            
            try
            {
                res = saveState(os);
            }
            catch (final InvalidAnnotationException ex)
            {
                ex.printStackTrace();  // TODO logging
                
                return false;
            }

            if (!res)  // no save_state/restore_state
            {
                res = true;
                
                if (_fields == null)
                {
                    Field[] fields = _theObject.getClass().getDeclaredFields(); // get all fields including private
                
                    _fields = new ArrayList<Field>();
                    
                    try
                    {
                        for (Field afield : fields)
                        {
                            // ignore if flagged with @NotState

                            if (!afield.isAnnotationPresent(NotState.class) && (!THIS_NAME.equals(afield.getName())))
                            {
                                /*
                                 * DO NOT try to save final values, since we cannot restore them
                                 * anyway! Also stay away from transients!
                                 */
                                
                                if (!((afield.getModifiers() & Modifier.FINAL) == Modifier.FINAL) &&
                                        !((afield.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT))
                                {
                                    _fields.add(afield);
                                }
                            }
                        }
                    }
                    catch (final Throwable ex)
                    {
                        res = false;
                    }
                }
                
                for (int i = 0; (i < _fields.size()) && res; i++)
                {
                    Field afield = _fields.get(i);
                    
                    afield.setAccessible(true);
    
                    /*
                     * TODO check that the user hasn't marked statics, finals etc.
                     */
                    
                    if (afield.getType().isArray())
                    {
                        res = packArray(afield, os);
                    }
                    else
                    {
                        if (afield.getType().isPrimitive())
                        {
                            res = packPrimitive(afield, os);
                        }
                        else
                            res = packObjectType(afield, os);
                    }
    
                    afield.setAccessible(false);
                }
            }
        }
        catch (final Throwable ex)
        {
            res = false;
        }
        
        return res;
    }
    
    public boolean restore_state (InputObjectState os, int ot)
    {
        if (!super.restore_state(os, ot))
            return false;      
        
        boolean res = false;
        
        try
        {
            /*
             * Priority is for @SaveState and @RestoreState first.
             */
            
            try
            {
                res = restoreState(os);
            }
            catch (final InvalidAnnotationException ex)
            {
                ex.printStackTrace();  // TODO logging
                
                return false;
            }
            
            if (!res)
            {
                res = true;
                
                if (_fields == null)
                {
                    Field[] fields = _theObject.getClass().getDeclaredFields(); // get all fields including private
                    
                    _fields = new ArrayList<Field>();
                    
                    try
                    {
                        for (Field afield : fields)
                        {
                            // ignore if flagged with @NotState
                            
                            if (!afield.isAnnotationPresent(NotState.class) && (!THIS_NAME.equals(afield.getName())))
                            {
                                /*
                                 * DO NOT try to restore final values! Or transients!
                                 */
                                
                                if (!((afield.getModifiers() & Modifier.FINAL) == Modifier.FINAL) &&
                                        !((afield.getModifiers() & Modifier.TRANSIENT) == Modifier.TRANSIENT))
                                {
                                    _fields.add(afield);
                                }
                            }
                        }
                    }
                    catch (final Throwable ex)
                    {
                        res = false;
                    }
                }
                
                for (int i = 0; (i < _fields.size()) && res; i++)
                {
                    Field afield = _fields.get(i);
                    
                    afield.setAccessible(true);
                    
                    /*
                     * TODO check that the user hasn't marked statics, finals etc.
                     */
                    
                    if (afield.getType().isArray())
                    {
                        res = unpackArray(afield, os);
                    }
                    else
                    {
                        if (afield.getType().isPrimitive())
                        {
                            res = unpackPrimitive(afield, os);
                        }
                        else
                            res = unpackObjectType(afield, os);
                    }
                    
                    afield.setAccessible(false);
                }
            }
        }
        catch (final Throwable ex)
        {
            res = false;
        }
        
        return res;
    }
    
    public String type ()
    {
        return "/StateManager/LockManager/"+_theObject.getClass().getCanonicalName();
    }
    
    public final RecoverableContainer<T> getContainer ()
    {
        return _container; 
    }
    
    private boolean packArray (final Field afield, OutputObjectState os)
    {
        if (!packPrimitiveArray(afield, os))
            return packObjectArray(afield, os);
        else
            return true;
    }
    
    private boolean packPrimitiveArray (final Field afield, OutputObjectState os)
    {
        boolean success = true;
        
        try
        {
            Class<?> c = afield.getType();
            final int size = Array.getLength(afield.get(_theObject));
            
            if (c.equals(int[].class))
            {
                final int[] objs = (int[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packInt(objs[i]);
            }
            else if (c.equals(boolean[].class))
            {
                final boolean[] objs = (boolean[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packBoolean(objs[i]);
            }
            else if (c.equals(byte[].class))
            {
                final byte[] objs = (byte[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packByte(objs[i]);
            }
            else if (c.equals(short[].class))
            {
                final short[] objs = (short[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packShort(objs[i]);
            }
            else if (c.equals(long[].class))
            {
                final long[] objs = (long[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packLong(objs[i]);
            }
            else if (c.equals(float[].class))
            {
                final float[] objs = (float[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packFloat(objs[i]);
            }
            else if (c.equals(double[].class))
            {
                final double[] objs = (double[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packDouble(objs[i]);
            }
            else if (c.equals(char[].class))
            {
                final char[] objs = (char[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    os.packChar(objs[i]);
            }
            else
                success = false;
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        return success;
    }
    
    private boolean packObjectArray (final Field afield, OutputObjectState os)
    {
        boolean success = true;
        
        try
        {
            Class<?> c = afield.getType();
            final int size = Array.getLength(afield.get(_theObject));
            
            if (c.equals(Integer[].class))
            {
                final Integer[] objs = (Integer[]) afield.get(_theObject);

                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);                    
                        os.packInt(objs[i]);
                    }
                }
            }
            else if (c.equals(Boolean[].class))
            {
                final Boolean[] objs = (Boolean[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packBoolean(objs[i]);
                    }
                }
            }
            else if (c.equals(Byte[].class))
            {
                final Byte[] objs = (Byte[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packByte(objs[i]);
                    }
                }
            }
            else if (c.equals(Short[].class))
            {
                final Short[] objs = (Short[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packShort(objs[i]);
                    }
                }
            }
            else if (c.equals(Long[].class))
            {
                final Long[] objs = (Long[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packLong(objs[i]);
                    }
                }
            }
            else if (c.equals(Float[].class))
            {
                final Float[] objs = (Float[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packFloat(objs[i]);
                    }
                }
            }
            else if (c.equals(Double[].class))
            {
                final Double[] objs = (Double[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packDouble(objs[i]);
                    }
                }
            }
            else if (c.equals(Character[].class))
            {
                final Character[] objs = (Character[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packChar(objs[i]);
                    }
                }
            }
            else if (c.equals(String[].class))
            {
                final String[] objs = (String[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    if (objs[i] == null)
                        os.packBoolean(false);
                    else
                    {
                        os.packBoolean(true);
                        os.packString(objs[i]);
                    }
                }
            }
            else
            {
                System.err.println("Array type "+c+" not supported!");
                
                success = false;
            }
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        return success;
    }
    
    private boolean unpackArray (final Field afield, InputObjectState os)
    {
        if (!unpackPrimitiveArray(afield, os))
            return unpackObjectArray(afield, os);
        else
            return true;
    }
    
    private boolean unpackPrimitiveArray (final Field afield, InputObjectState os)
    {
        boolean success = true;
        
        try
        {
            Class<?> c = afield.getType();
            final int size = Array.getLength(afield.get(_theObject));
            
            if (c.equals(int[].class))
            {
                final int[] objs = (int[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackInt();
            }
            else if (c.equals(boolean[].class))
            {
                final boolean[] objs = (boolean[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackBoolean();
            }
            else if (c.equals(byte[].class))
            {
                final byte[] objs = (byte[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackByte();
            }
            else if (c.equals(short[].class))
            {
                final short[] objs = (short[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackShort();
            }
            else if (c.equals(long[].class))
            {
                final long[] objs = (long[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackLong();
            }
            else if (c.equals(float[].class))
            {
                final float[] objs = (float[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackFloat();
            }
            else if (c.equals(double[].class))
            {
                final double[] objs = (double[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackDouble();
            }
            else if (c.equals(char[].class))
            {
                final char[] objs = (char[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                    objs[i] = os.unpackChar();
            }
            else
                success = false;
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        return success;
    }
    
    private boolean unpackObjectArray (final Field afield, InputObjectState os)
    {
        boolean success = true;
        
        try
        {
            Class<?> c = afield.getType();
            final int size = Array.getLength(afield.get(_theObject));
            
            if (c.equals(Integer[].class))
            {
                final Integer[] objs = (Integer[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackInt();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Boolean[].class))
            {
                final Boolean[] objs = (Boolean[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackBoolean();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Byte[].class))
            {
                final Byte[] objs = (Byte[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackByte();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Short[].class))
            {
                final Short[] objs = (Short[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackShort();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Long[].class))
            {
                final Long[] objs = (Long[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackLong();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Float[].class))
            {
                final Float[] objs = (Float[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackFloat();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Double[].class))
            {
                final Double[] objs = (Double[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackDouble();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(Character[].class))
            {
                final Character[] objs = (Character[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackChar();
                    else
                        objs[i] = null;
                }
            }
            else if (c.equals(String[].class))
            {
                final String[] objs = (String[]) afield.get(_theObject);
                
                for (int i = 0; (i < size) && success; i++)
                {
                    boolean ref = os.unpackBoolean();
                    
                    if (ref)
                        objs[i] = os.unpackString();
                    else
                        objs[i] = null;
                }
            }
            else
            {
                System.err.println("Array type "+c+" not supported!");
                
                success = false;
            }
        }
        catch (final Throwable ex)
        {
            ex.printStackTrace();
            
            success = false;
        }
        
        return success;
    }
    
    private boolean packPrimitive (final Field afield, OutputObjectState os)
    {
        try
        {
            /*
             * TODO deal with arrays of primitive types.
             * 
             * Workaround - provide saveState and restoreState annotations.
             */
            
            if (afield.getType().equals(Boolean.TYPE))
                os.packBoolean(afield.getBoolean(_theObject));
            else if (afield.getType().equals(Byte.TYPE))
                os.packByte(afield.getByte(_theObject));
            else if (afield.getType().equals(Short.TYPE))
                os.packShort(afield.getShort(_theObject));
            else if (afield.getType().equals(Integer.TYPE))
                os.packInt(afield.getInt(_theObject));
            else if (afield.getType().equals(Long.TYPE))
                os.packLong(afield.getLong(_theObject));
            else if (afield.getType().equals(Float.TYPE))
                os.packFloat(afield.getFloat(_theObject));
            else if (afield.getType().equals(Double.TYPE))
                os.packDouble(afield.getDouble(_theObject));
            else if (afield.getType().equals(Character.TYPE))
                os.packChar(afield.getChar(_theObject));
            else
                return false;
        }
        catch (final IOException ex)
        {
            return false;
        }
        catch (final Exception ex)
        {
            return false;
        }
        
        return true;
    }
    
    private boolean packObjectType (final Field afield, OutputObjectState os)
    {
        try
        {
            if (afield.getType().equals(Boolean.class))
                os.packBoolean((Boolean) afield.get(_theObject));
            else if (afield.getType().equals(Byte.class))
                os.packByte((Byte) afield.get(_theObject));
            else if (afield.getType().equals(Short.class))
                os.packShort((Short) afield.get(_theObject));
            else if (afield.getType().equals(Integer.class))
                os.packInt((Integer) afield.get(_theObject));
            else if (afield.getType().equals(Long.class))
                os.packLong((Long) afield.get(_theObject));
            else if (afield.getType().equals(Float.class))
                os.packFloat((Float) afield.get(_theObject));
            else if (afield.getType().equals(Double.class))
                os.packDouble((Double) afield.get(_theObject));
            else if (afield.getType().equals(Character.class))
                os.packChar((Character) afield.get(_theObject));
            else if (afield.getType().equals(String.class))
                os.packString((String) afield.get(_theObject));
            else if (afield.getType().isAnnotationPresent(Transactional.class))
                return packTransactionalInstance(afield, os);
            else
                return false;
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    /*
     * This only works if this type and the types we're packing share the same container.
     * So we need a way to specify (or determine) the container for all transactional
     * instances.
     */
    
    @SuppressWarnings("unchecked")
    private boolean packTransactionalInstance (final Field afield, OutputObjectState os)
    {
        Object ptr = null;
        
        try
        {
            ptr = afield.get(_theObject);
            
            if (ptr == null)
            {
                os.packBoolean(false);
            }
            else
            {
                os.packBoolean(true);
                UidHelper.packInto(_container.getUidForHandle((T) ptr), os);
            }
        }
        catch (final ClassCastException ex)
        {
            System.err.println("Field "+ptr+" is not a transactional instance!");
            
            return false;
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    private boolean saveState (OutputObjectState os) throws InvalidAnnotationException
    {
        boolean res = false;
        
        checkValidity(_theObject.getClass());
        
        if (_saveState != null)
        {
            try
            {
                _saveState.invoke(_theObject, os);
                
                res = true;
            }
            catch (final Throwable ex)
            {
                ex.printStackTrace();
            }
        }
        
        return res;
    }
    
    private boolean restoreState (InputObjectState os) throws InvalidAnnotationException
    {
        boolean res = false;
        
        checkValidity(_theObject.getClass());
        
        if (_restoreState != null)
        {
            try
            {
                _restoreState.invoke(_theObject, os);
                
                res = true;
            }
            catch (final Throwable ex)
            {
                ex.printStackTrace();
            }
        }
        
        return res;
    }
    
    private void checkValidity (Class<?> toCheck) throws InvalidAnnotationException
    {
        if (_checkSaveRestore)
            return;
        
        try
        {
            Method[] methods = toCheck.getDeclaredMethods();
    
            if (methods != null)
            {
                for (Method mt : methods)
                {
                    if ((mt.isAnnotationPresent(SaveState.class) && (_saveState == null)))
                    {
                        _saveState = mt;
                    }
                    
                    if ((mt.isAnnotationPresent(RestoreState.class) && (_restoreState == null)))
                    {
                        _restoreState = mt;
                    }
                }
            }
            
            if ((_saveState != null) && (_restoreState != null))
            {
                return;
            }
            else
            {
                if ((_restoreState == null) && (_saveState == null))
                {
                    Class<?> superClass = toCheck.getSuperclass();
    
                    if (superClass != Object.class)
                        checkValidity(superClass);
                }
                else
                    throw new InvalidAnnotationException("WARNING: both save_state and restore_state are not present!");
            }
        }
        finally
        {      
            _checkSaveRestore = true;
        }
    }
    
    private boolean unpackPrimitive (final Field afield, InputObjectState os)
    {
        try
        {
            // TODO arrays

            if (afield.getType().equals(Boolean.TYPE))
                afield.setBoolean(_theObject, os.unpackBoolean());
            else if (afield.getType().equals(Byte.TYPE))
                afield.setByte(_theObject, os.unpackByte());
            else if (afield.getType().equals(Short.TYPE))
                afield.setShort(_theObject, os.unpackShort());
            else if (afield.getType().equals(Integer.TYPE))
                afield.setInt(_theObject, os.unpackInt());
            else if (afield.getType().equals(Long.TYPE))
                afield.setLong(_theObject, os.unpackLong());
            else if (afield.getType().equals(Float.TYPE))
                afield.setFloat(_theObject, os.unpackFloat());
            else if (afield.getType().equals(Double.TYPE))
                afield.setDouble(_theObject, os.unpackDouble());
            else if (afield.getType().equals(Character.TYPE))
                afield.setChar(_theObject, os.unpackChar());
            else
                return false;
        }
        catch (final IOException ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    private boolean unpackObjectType (final Field afield, InputObjectState os)
    {
        try
        {
            // TODO arrays
            
            if (afield.getType().equals(Boolean.class))
                afield.set(_theObject, os.unpackBoolean());
            else if (afield.getType().equals(Byte.class))
                afield.set(_theObject, os.unpackByte());
            else if (afield.getType().equals(Short.class))
                afield.set(_theObject, os.unpackShort());
            else if (afield.getType().equals(Integer.class))
                afield.set(_theObject, os.unpackInt());
            else if (afield.getType().equals(Long.class))
                afield.set(_theObject, os.unpackLong());
            else if (afield.getType().equals(Float.class))
                afield.set(_theObject, os.unpackFloat());
            else if (afield.getType().equals(Double.class))
                afield.set(_theObject, os.unpackDouble());
            else if (afield.getType().equals(Character.class))
                afield.set(_theObject, os.unpackChar());
            else if (afield.getType().equals(String.class))
                afield.set(_theObject, os.unpackString());
            else if (afield.getType().isAnnotationPresent(Transactional.class))
                return unpackTransactionalInstance(afield, os);
            else
                return false;
        }
        catch (final IOException ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        
        return true;
    }
    
    /*
     * This only works if this type and the types we're packing share the same container.
     * So we need a way to specify (or determine) the container for all transactional
     * instances.
     */
    
    private boolean unpackTransactionalInstance (final Field afield, InputObjectState os)
    {
        try
        {
            boolean ptr = os.unpackBoolean();
            
            if (!ptr)
                afield.set(_theObject, null);
            else
            {
                Uid u = UidHelper.unpackFrom(os);
                
                afield.set(_theObject, _container.getHandle(u));
            }
        }
        catch (final Exception ex)
        {
            ex.printStackTrace();
            
            return false;
        }
        
        return true;
    }

    // the object we are working on.
    
    private T _theObject;
    
    // the cached methods/fields
    
    private boolean _checkSaveRestore = false;
    private Method _saveState = null;
    private Method _restoreState = null;
    private RecoverableContainer<T> _container = null;
    
    private ArrayList<Field> _fields = null;
    
    private static final String THIS_NAME = "this$0";  // stop us trying to pack this!
}