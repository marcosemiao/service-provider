/*
 * Copyright 2015 Marco Semiao
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package fr.ms.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;

/**
 * Original class for Service Provider Java 3
 *
 * @see <a href="http://marcosemiao4j.wordpress.com">Marco4J</a>
 *
 *
 * @author Marco Semiao
 *
 */
public final class Service {

    private static final String PREFIX = "META-INF/services/";

    private static void fail(final Class paramClass, final String paramString) throws ServiceConfigurationError {
	throw new ServiceConfigurationError(paramClass.getName() + ": " + paramString);
    }

    private static void fail(final Class paramClass, final URL paramURL, final int paramInt, final String paramString) throws ServiceConfigurationError {
	fail(paramClass, paramURL + ":" + paramInt + ": " + paramString);
    }

    private static int parseLine(final Class paramClass, final URL paramURL, final BufferedReader paramBufferedReader, final int paramInt,
	    final List paramList, final Set paramSet) throws IOException, ServiceConfigurationError {
	String str = paramBufferedReader.readLine();
	if (str == null) {
	    return -1;
	}
	final int i = str.indexOf('#');
	if (i >= 0) {
	    str = str.substring(0, i);
	}
	str = str.trim();
	final int j = str.length();
	if (j != 0) {
	    if ((str.indexOf(' ') >= 0) || (str.indexOf('\t') >= 0)) {
		fail(paramClass, paramURL, paramInt, "Illegal configuration-file syntax");
	    }
	    if (!Character.isJavaIdentifierStart(str.charAt(0))) {
		fail(paramClass, paramURL, paramInt, "Illegal provider-class name: " + str);
	    }
	    for (int k = 1; k < j; k++) {
		final char c = str.charAt(k);
		if ((!Character.isJavaIdentifierPart(c)) && (c != '.')) {
		    fail(paramClass, paramURL, paramInt, "Illegal provider-class name: " + str);
		}
	    }
	    if (!paramSet.contains(str)) {
		paramList.add(str);
		paramSet.add(str);
	    }
	}
	return paramInt + 1;
    }

    private static Iterator parse(final Class paramClass, final URL paramURL, final Set paramSet) throws ServiceConfigurationError {
	InputStream localInputStream = null;
	BufferedReader localBufferedReader = null;
	final ArrayList localArrayList = new ArrayList();
	try {
	    localInputStream = paramURL.openStream();
	    localBufferedReader = new BufferedReader(new InputStreamReader(localInputStream, "utf-8"));
	    int i = 1;
	    while ((i = parseLine(paramClass, paramURL, localBufferedReader, i, localArrayList, paramSet)) >= 0) {
		;
	    }
	} catch (final IOException localIOException1) {
	    fail(paramClass, ": " + localIOException1);
	} finally {
	    try {
		if (localBufferedReader != null) {
		    localBufferedReader.close();
		}
		if (localInputStream != null) {
		    localInputStream.close();
		}
	    } catch (final IOException localIOException2) {
		fail(paramClass, ": " + localIOException2);
	    }

	}
	return localArrayList.iterator();
    }

    public static Iterator providers(final Class paramClass, final ClassLoader paramClassLoader) throws ServiceConfigurationError {
	return new LazyIterator(paramClass, paramClassLoader);
    }

    public static Iterator providers(final Class paramClass) throws ServiceConfigurationError {
	final ClassLoader localClassLoader = Thread.currentThread().getContextClassLoader();
	return providers(paramClass, localClassLoader);
    }

    public static Iterator installedProviders(final Class paramClass) throws ServiceConfigurationError {
	ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
	if (localClassLoader != null) {
	    localClassLoader = localClassLoader.getParent();
	}
	return providers(paramClass, localClassLoader);
    }

    private static class LazyIterator implements Iterator {
	Class service;
	ClassLoader loader;
	Enumeration configs = null;
	Iterator pending = null;
	Set returned = new TreeSet();
	String nextName = null;

	private LazyIterator(final Class paramClass, final ClassLoader paramClassLoader) {
	    this.service = paramClass;
	    this.loader = paramClassLoader;
	}

	public boolean hasNext() throws ServiceConfigurationError {
	    if (this.nextName != null) {
		return true;
	    }
	    if (this.configs == null) {
		try {
		    final String str = PREFIX + this.service.getName();
		    if (this.loader == null) {
			this.configs = ClassLoader.getSystemResources(str);
		    } else {
			this.configs = this.loader.getResources(str);
		    }
		} catch (final IOException localIOException) {
		    Service.fail(this.service, ": " + localIOException);
		}
	    }
	    while ((this.pending == null) || (!this.pending.hasNext())) {
		if (!this.configs.hasMoreElements()) {
		    return false;
		}
		this.pending = Service.parse(this.service, (URL) this.configs.nextElement(), this.returned);
	    }
	    this.nextName = ((String) this.pending.next());
	    return true;
	}

	public Object next() throws ServiceConfigurationError {
	    if (!hasNext()) {
		throw new NoSuchElementException();
	    }
	    final String str = this.nextName;
	    this.nextName = null;
	    try {
		return Class.forName(str, true, this.loader).newInstance();
	    } catch (final ClassNotFoundException localClassNotFoundException) {
		Service.fail(this.service, "Provider " + str + " not found");
	    } catch (final Exception localException) {
		Service.fail(this.service, "Provider " + str + " could not be instantiated: " + localException);
	    }

	    return null;
	}

	public void remove() {
	    throw new UnsupportedOperationException();
	}
    }

    public static class ServiceConfigurationError extends Error {

	private static final long serialVersionUID = 1L;

	public ServiceConfigurationError(final String paramString) {
	    super(paramString);
	}
    }
}
