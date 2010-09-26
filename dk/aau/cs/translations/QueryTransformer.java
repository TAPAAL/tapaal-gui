package dk.aau.cs.translations;

public interface QueryTransformer<TInput, TOutput> {
	TOutput transformQuery(TInput query) throws Exception;
}
