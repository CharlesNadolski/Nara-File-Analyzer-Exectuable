package edu.georgetown.library.fileAnalyzer.counter;

public enum CounterStat {
	VALID,   //Cell fonforms to rule
	WARNING, //Cell does not conform to spec, but issue occurrs so frequently it is being passed
	INVALID_CASE, //Cell has case mismatch
	INVALID_TRIM, //Cell has extra whitespace
	INVALID, //Cell does not conform to rule
	ERROR,   //Processing error interpreting rule
	UNSUPPORTED_REPORT,   //Code not yet implemented for this report type
	UNKNOWN_REPORT_TYPE,  //Report type does not match know types
	UNSUPPORTED_FILE,     //File cannot be broken into cells for processing
}