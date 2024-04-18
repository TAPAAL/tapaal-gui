package dk.aau.cs.pddl;

public class PddlStringifier {
    private Model model;

    public PddlStringifier(Model model) {
        this.model = model;
    }

    public String getString() {
        return buildModel().toString();
    }


    public StringBuilder buildModel() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("(define (domain %s)\n", model.getName()));

        sb.append(this.buildExtensions());
        sb.append(this.buildTypes());
        sb.append(this.buildFunctions());
        sb.append(this.buildActions());
        sb.append(")");

        return sb;
    }

    public StringBuilder buildExtensions() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:requirements");
        for(Extension extension : model.getExtensions()) {
            sb.append(String.format(" :%s", extension.getName()));
        }
        sb.append(")\n");

        return sb;
    }

    public StringBuilder buildTypes() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:types\n");
        for(UserType type : model.getTypes().values()) {
            sb.append(String.format("\t\t:%s\n", type.getName()));
        }
        sb.append("\t)\n");

        return sb;
    }

    public StringBuilder buildFunctions() {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:functions\n");
        for(FunctionSignature function : model.getFunctions().values()) {
            sb.append(String.format("\t\t(%s%s)\n",
                function.getName(),
                this.buildParameters(function.getParameters()).toString()
            ));
        }
        sb.append("\t)\n");

        return sb;
    }

    public StringBuilder buildParameters(Iterable<Parameter> parameters) {
        StringBuilder sb = new StringBuilder();

        for(Parameter param : parameters) {
            sb.append(String.format(" ?%s - %s",
                param.getName(),
                param.getType().getName()
            ));
        }

        return sb;
    }

    public StringBuilder buildActions() {
        StringBuilder sb = new StringBuilder();

        for (ActionSchema action : model.getActionSchemas().values()) {
            sb.append(buildAction(action));
        }

        return sb;
    }

    public StringBuilder buildAction(ActionSchema actionSchema) {
        StringBuilder sb = new StringBuilder();

        sb.append("\t(:action ").append(actionSchema.getName()).append("\n");

        sb.append("\t\t(:parameters").append(buildParameters(actionSchema.getParameters().values())).append(")\n");

        sb.append("\t\t(:precondition ").append(actionSchema.getPrecondition().toString()).append(")\n");

        sb.append("\t\t(:effects ").append(actionSchema.getEffects().toString()).append(")\n");

        sb.append("\t)\n");

        return sb;
    }




}
