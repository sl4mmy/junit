/**
 * 
 */
package org.junit.runner.internal.request;

import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.extensions.Filter;

public final class FilterRequest extends Request {
	private final Request fRequest;

	private final Filter fFilter;

	public FilterRequest(Request classRequest, Filter filter) {
		fRequest = classRequest;
		fFilter = filter;
	}

	@Override
	public Runner getRunner() {
		Runner runner = fFilter.apply(fRequest.getRunner());
		if (runner.testCount() == 0)
			return Request.anErrorReport(Filter.class, new Exception(String
					.format("No tests found matching %s from %s", fFilter
							.describe(), fRequest.toString()))).getRunner();
		return runner;
	}
}