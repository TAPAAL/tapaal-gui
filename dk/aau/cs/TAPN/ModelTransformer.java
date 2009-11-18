package dk.aau.cs.TAPN;

public interface ModelTransformer<TInput, TOutput> {
	TOutput transform(TInput model) throws Exception;
}
