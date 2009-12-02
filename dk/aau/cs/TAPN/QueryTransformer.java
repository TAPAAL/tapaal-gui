package dk.aau.cs.TAPN;

public interface QueryTransformer<TInput, TOutput> {
	TOutput transformQuery(TInput query) throws Exception;
}
