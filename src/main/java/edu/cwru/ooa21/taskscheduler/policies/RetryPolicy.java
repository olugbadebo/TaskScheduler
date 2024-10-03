package edu.cwru.ooa21.taskscheduler.policies;

public class RetryPolicy
{
    private final int maxRetries;
    private final long delayInMillis;
    private final boolean useExponentialBackoff;

    public RetryPolicy(int maxRetries, long delayInMillis, boolean useExponentialBackoff)
    {
        this.maxRetries = maxRetries;
        this.delayInMillis = delayInMillis;
        this.useExponentialBackoff = useExponentialBackoff;
    }

    public int getMaxRetries()
    {
        return maxRetries;
    }

    public long getDelayInMillis(int attempt)
    {
        if (useExponentialBackoff) {
            return delayInMillis * (1L << (attempt - 1));
        }
        return delayInMillis;
    }
}
