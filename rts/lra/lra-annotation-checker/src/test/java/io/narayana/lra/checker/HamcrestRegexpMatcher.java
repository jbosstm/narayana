/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2017, Red Hat Middleware LLC, and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package io.narayana.lra.checker;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.StreamSupport;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;

/**
 * <p>
 * Hamcrest checker which handles regular expressions.
 * <p>
 * Checking on {@link String} or if some record of {@link Iterable} matches the regular expression.
 *
 * @author Ondra Chaloupka <ochaloup@redhat.com>
 */
public class HamcrestRegexpMatcher extends BaseMatcher<Object> {
    private final Pattern regexpPattern;
    private static final String MATCH_ALL_REGEX = ".*";

    /**
     * Public API for this matcher. Expecting regular expression
     * to be then checked.
     *
     * @param regexp  regular expression
     * @return instance of hamcrest matcher
     */
    public static HamcrestRegexpMatcher matches(final String regexp) {
        return new HamcrestRegexpMatcher(regexp);
    }

    private HamcrestRegexpMatcher(final String regexpString) {
        String tunedRegexpString = regexpString;
        if (!regexpString.startsWith(MATCH_ALL_REGEX)) {
            tunedRegexpString = MATCH_ALL_REGEX + tunedRegexpString;
        }
        if (!regexpString.endsWith(MATCH_ALL_REGEX)) {
            tunedRegexpString = tunedRegexpString + MATCH_ALL_REGEX;
        }
        this.regexpPattern = Pattern.compile(tunedRegexpString);
    }

    @Override
    public boolean matches(Object objectToMatch) {
        if ((objectToMatch instanceof String)) {
            return regexpPattern.matcher((String) objectToMatch).matches();
        }
        if ((objectToMatch instanceof String[])) {
            return matchOverItrable(Arrays.asList((String[]) objectToMatch));
        }
        if ((objectToMatch instanceof Iterable<?>)) {
            return matchOverItrable((Iterable<?>) objectToMatch);
        }
        return false;
    }

    @Override
    public void describeTo(Description description) {
        description.appendText(String.format("matches regexp '%s'", regexpPattern));
    }


    private boolean matchOverItrable(Iterable<?> iterable) {
        return StreamSupport.stream(iterable.spliterator(), false)
                .anyMatch(record -> regexpPattern.matcher(record.toString()).matches());
    }
}
