package dk.aau.cs.TAPN;

public interface ModelTransformer<TInput, TOutput> {
	TOutput transformModel(TInput model) throws Exception;
}
