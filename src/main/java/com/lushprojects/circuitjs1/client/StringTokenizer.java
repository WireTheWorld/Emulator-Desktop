

package com.lushprojects.circuitjs1.client;

import java.util.Enumeration;
import java.util.NoSuchElementException;

/* StringTokenizer -- breaks a String into tokens
Copyright (C) 1998, 1999, 2001, 2002, 2005  Free Software Foundation, Inc.

This file is part of GNU Classpath.

GNU Classpath is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; either version 2, or (at your option)
any later version.

GNU Classpath is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
General Public License for more details.

You should have received a copy of the GNU General Public License
along with GNU Classpath; see the file COPYING.  If not, write to the
Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA
02110-1301 USA.

Linking this library statically or dynamically with other modules is
making a combined work based on this library.  Thus, the terms and
conditions of the GNU General Public License cover the whole
combination.

As a special exception, the copyright holders of this library give you
permission to link this library with independent modules to produce an
executable, regardless of the license terms of these independent
modules, and to copy and distribute the resulting executable under
terms of your choice, provided that you also meet, for each linked
independent module, the terms and conditions of the license of that
module.  An independent module is a module which is not derived from
or based on this library.  If you modify this library, you may extend
this exception to your version of the library, but you are not
obligated to do so.  If you do not wish to do so, delete this
exception statement from your version. */



/**
* 此类将字符串拆分为标记。调用者可以设置在哪些分隔符处拆分字符串，
* 以及是否返回分隔符。这比 {@link java.io.StreamTokenizer} 简单得多。
*
* <p>你可以通过调用 nextToken(String) 动态更改分隔符集合。但其语义
* 相当复杂；它甚至取决于是否调用过 <code>hasMoreTokens()</code>。
* 你应该先调用 <code>hasMoreTokens()</code>，否则最后一个标记之后的
* 旧分隔符可能会被返回。
*
* <p>如果你想获取分隔符，必须使用三参数构造函数。分隔符会作为仅含
* 单个字符的标记返回。
*
* @author Jochen Hoenicke
* @author Warren Levy (warrenl@cygnus.com)
* @see java.io.StreamTokenizer
* @status updated to 1.4
*/
public class StringTokenizer implements Enumeration<Object>
{
// 警告：StringTokenizer 是引导周期中的核心类。有关这一事实的影响，
// 请参阅 vm/reference/java/lang/Runtime 中的注释。

/**
* 我们在 str 中当前所处的位置。
*/
private int pos;

/**
* 需要拆分为标记的字符串。
*/
private final String str;

/**
* 字符串的长度。
*/
private final int len;

/**
* 包含分隔符字符的字符串。
*/
private String delim;

/**
* 指示我们是否应返回分隔符。
*/
private final boolean retDelims;

/**
* 为字符串 <code>str</code> 创建一个新的 StringTokenizer，它将按默认
* 分隔符集合（空格、制表符、换行、回车和换页符）拆分，且不返回分隔符。
*
* @param str The string to split
* @throws NullPointerException if str is null
*/
public StringTokenizer(String str)
{
 this(str, " \t\n\r\f", false);
}

/**
* 创建一个新的 StringTokenizer，按给定的分隔符字符拆分给定字符串。
* 它不返回分隔符字符。
*
* @param str the string to split
* @param delim a string containing all delimiter characters
* @throws NullPointerException if either argument is null
*/
public StringTokenizer(String str, String delim)
{
 this(str, delim, false);
}

/**
* 创建一个新的 StringTokenizer，按给定的分隔符字符拆分给定字符串。
* 如果将 <code>returnDelims</code> 设置为 <code>true</code>，分隔符
* 字符将作为独立的标记返回。分隔符标记始终由单个字符组成。
*
* @param str the string to split
* @param delim a string containing all delimiter characters
* @param returnDelims tells, if you want to get the delimiters
* @throws NullPointerException if str or delim is null
*/
public StringTokenizer(String str, String delim, boolean returnDelims)
{
 len = str.length();
 this.str = str;
 this.delim = delim;
 this.retDelims = returnDelims;
 this.pos = 0;
}

/**
* 指示是否还有更多标记。
*
* @return true if the next call of nextToken() will succeed
*/
public boolean hasMoreTokens()
{
 if (! retDelims)
   {
     while (pos < len && delim.indexOf(str.charAt(pos)) >= 0)
       pos++;
   }
 return pos < len;
}

/**
* 返回下一个标记，并将分隔符集合更改为给定的 <code>delim</code>。
* 分隔符集合的更改是永久性的，即下一次调用 nextToken() 时使用相同的
* 分隔符集合。
*
* @param delim a string containing the new delimiter characters
* @return the next token with respect to the new delimiter characters
* @throws NoSuchElementException if there are no more tokens
* @throws NullPointerException if delim is null
*/
public String nextToken(String delim) throws NoSuchElementException
{
 this.delim = delim;
 return nextToken();
}

/**
* 返回字符串的下一个标记。
*
* @return the next token with respect to the current delimiter characters
* @throws NoSuchElementException if there are no more tokens
*/
public String nextToken() throws NoSuchElementException
{
 if (pos < len && delim.indexOf(str.charAt(pos)) >= 0)
   {
     if (retDelims)
       return str.substring(pos, ++pos);
     while (++pos < len && delim.indexOf(str.charAt(pos)) >= 0)
       ;
   }
 if (pos < len)
   {
     int start = pos;
     while (++pos < len && delim.indexOf(str.charAt(pos)) < 0)
       ;

     return str.substring(start, pos);
   }
 throw new NoSuchElementException();
}

/**
* 此方法与 hasMoreTokens 作用相同。这是 <code>Enumeration</code> 接口方法。
*
* @return true, if the next call of nextElement() will succeed
* @see #hasMoreTokens()
*/
public boolean hasMoreElements()
{
 return hasMoreTokens();
}

/**
* 此方法与 nextTokens 作用相同。这是 <code>Enumeration</code> 接口方法。
*
* @return the next token with respect to the current delimiter characters
* @throws NoSuchElementException if there are no more tokens
* @see #nextToken()
*/
public Object nextElement() throws NoSuchElementException
{
 return nextToken();
}

/**
* 此方法统计字符串中相对于当前分隔符集合的剩余标记数量。
*
* @return the number of times <code>nextTokens()</code> will succeed
* @see #nextToken()
*/
public int countTokens()
{
 int count = 0;
 int delimiterCount = 0;
 boolean tokenFound = false; // 当找到非分隔符时置为 true
 int tmpPos = pos;

 // 为提高效率，我们累计分隔符数量，而不是每遇到一个就检查
 // retDelims。这样我们只需在方法末尾做一次条件判断
 while (tmpPos < len)
   {
     if (delim.indexOf(str.charAt(tmpPos++)) >= 0)
       {
         if (tokenFound)
           {
             // 已到达标记末尾
             count++;
             tokenFound = false;
           }
         delimiterCount++; // 为该分隔符计数加一
       }
     else
       {
         tokenFound = true;
         // 到达标记末尾
         while (tmpPos < len
                && delim.indexOf(str.charAt(tmpPos)) < 0)
           ++tmpPos;
       }
   }

 // 确保统计最后一个标记
 if (tokenFound)
   count++;

 // 如果统计分隔符，则将其计入标记总数
 return retDelims ? count + delimiterCount : count;
}
} // class StringTokenizer
