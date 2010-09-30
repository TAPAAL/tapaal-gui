package pipe.gui.widgets;


import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JRootPane;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import dk.aau.cs.TCTL.*;

import pipe.dataLayer.DataLayer;
import pipe.dataLayer.TAPNQuery;
import pipe.dataLayer.TAPNQuery.ReductionOption;
import pipe.dataLayer.TAPNQuery.SearchOption;
import pipe.dataLayer.TAPNQuery.TraceOption;
import pipe.gui.CreateGui;
import pipe.gui.Export;
import pipe.gui.Pipe;
import pipe.gui.Verifier;

public class QueryDialogue extends JPanel{

	//private static final Font PROPERTY_FONT = new Font("Ariel", Font.BOLD, 14);
	//private static final Color SELECTED_BG_COLOR = new Color(255, 240, 153);
	private static final long serialVersionUID = 7852107237344005546L;
	public enum QueryDialogueOption {VerifyNow, Save, Export}

	private boolean querySaved = false;

	private JRootPane rootPane;

	// Query Name Panel;
	private JPanel namePanel;

	// Boundedness check panel
	private JPanel boundednessCheckPanel;
	private JSpinner numberOfExtraTokensInNet;
	private JButton kbounded;
	private JButton kboundedOptimize;

	// Query Panel
	private JPanel queryPanel;

	private JPanel quantificationPanel;
	private ButtonGroup quantificationRadioButtonGroup;
	private JRadioButton existsDiamond;
	private JRadioButton existsBox;
	private JRadioButton forAllDiamond;
	private JRadioButton forAllBox;	

	private JTextField queryField;

	private JPanel logicButtonPanel;
	private ButtonGroup logicButtonGroup;
	private JButton conjunctionButton;
	private JButton disjunctionButton;

	private JPanel predicatePanel;
	private JButton addPredicateButton;
	private JComboBox placesBox;
	private JComboBox relationalOperatorBox;
	private JSpinner placeMarking;



	// Uppaal options panel (search + trace options)
	// search options panel
	private JPanel searchOptionsPanel;
	private JPanel uppaalOptionsPanel;
	private ButtonGroup searchRadioButtonGroup;
	private JRadioButton bFS;
	private JRadioButton dFS;
	private JRadioButton rDFS;
	private JRadioButton closestToTargetFirst;

	// Trace options panel
	private JPanel traceOptionsPanel;

	private ButtonGroup traceRadioButtonGroup;
	private JRadioButton none;
	private JRadioButton some;
	private JRadioButton fastest;

	// Reduction options panel
	private JPanel reductionOptionsPanel;
	private JComboBox reductionOption;
	private JCheckBox symmetryReduction;

	// Buttons in the bottom of the dialogue
	private JPanel buttonPanel;
	private JButton cancelButton;
	private JButton saveButton;
	private JButton saveAndVerifyButton;
	private JButton removeButton;
	private JButton saveUppaalXMLButton;

	// Private Members
	private StringPosition currentSelection = null;

	private DataLayer datalayer;
	private EscapableDialog me;

	private HashMap<JPanel, ActionListener> andActionListenerMap;

	private String name_ADVNOSYM = "Optimised Standard";
	private String name_NAIVE = "Standard";
	private String name_BROADCAST = "Broadcast Reduction";
	private String name_BROADCASTDEG2 = "Broadcast Degree 2 Reduction";

	private TCTLAbstractProperty newProperty;


	public QueryDialogue (EscapableDialog me, DataLayer datalayer, QueryDialogueOption option, TAPNQuery queryToCreateFrom){

		this.datalayer = datalayer;
		this.me = me;
		this.newProperty = queryToCreateFrom==null ? new TCTLPathPlaceHolder() : queryToCreateFrom.getProperty();
		andActionListenerMap = new HashMap<JPanel, ActionListener>();
		rootPane = me.getRootPane();
		setLayout(new GridBagLayout());

		init(option, queryToCreateFrom);
	}

	private void init(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		initQueryNamePanel(queryToCreateFrom);
		initBoundednessCheckPanel(queryToCreateFrom);
		initQueryPanel(queryToCreateFrom);
		initUppaalOptionsPanel(queryToCreateFrom);
		initReductionOptionsPanel(queryToCreateFrom);
		initButtonPanel(option,queryToCreateFrom);

		rootPane.setDefaultButton(saveButton);
		disableAllQueryButtons();
		setSaveButtonsEnabled();
	}

	private void initQueryNamePanel(final TAPNQuery queryToCreateFrom) {
		// Query comment field starts here:		
		namePanel = new JPanel(new FlowLayout());
		namePanel.add(new JLabel("Query comment: "));
		JTextField queryComment;
		if (queryToCreateFrom==null){
			queryComment = new JTextField("Query Comment/Name Here",25);
		}else{
			queryComment = new JTextField(queryToCreateFrom.getName(),25);	
		}

		namePanel.add(queryComment);

		queryComment.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void insertUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void changedUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}
		});



		GridBagConstraints gridBagConstraints = new GridBagConstraints();		
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(namePanel, gridBagConstraints);
	}

	private void initBoundednessCheckPanel(final TAPNQuery queryToCreateFrom) {

		// Number of extra tokens field
		boundednessCheckPanel = new JPanel(new FlowLayout());
		boundednessCheckPanel.add(new JLabel("Extra number of tokens: "));

		if (queryToCreateFrom == null){
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(3,0,Integer.MAX_VALUE, 1));
		}else{
			numberOfExtraTokensInNet = new JSpinner(new SpinnerNumberModel(queryToCreateFrom.getCapacity(),0,Integer.MAX_VALUE, 1));
		}
		numberOfExtraTokensInNet.setMaximumSize(new Dimension(50,30));
		numberOfExtraTokensInNet.setMinimumSize(new Dimension(50,30));
		numberOfExtraTokensInNet.setPreferredSize(new Dimension(50,30));
		boundednessCheckPanel.add(numberOfExtraTokensInNet);

		// Boundedness button
		kbounded = new JButton("Check Boundedness");
		kbounded.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						Verifier.analyseKBounded(CreateGui.getModel(), getCapacity());
					}

				}
		);		
		boundednessCheckPanel.add(kbounded);

		kboundedOptimize = new JButton("Optimize Number of Tokens");
		kboundedOptimize.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent evt){
						Verifier.analyzeAndOptimizeKBound(CreateGui.getModel(), getCapacity(), numberOfExtraTokensInNet);
					}
				}
		);
		boundednessCheckPanel.add(kboundedOptimize);

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		add(boundednessCheckPanel, gridBagConstraints);
	}

	private void initQueryPanel(final TAPNQuery queryToCreateFrom) {
		queryPanel = new JPanel(new GridBagLayout());
		queryPanel.setBorder(BorderFactory.createTitledBorder("Query"));

		// Query Text Field
		queryField = new JTextField();
		
		
		queryField.setText(newProperty.toString());
		queryField.setHorizontalAlignment(JTextField.CENTER);
		queryField.setEditable(false);
		queryField.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				updateSelection();
			}
		}
		);	

		queryField.getDocument().addDocumentListener(new DocumentListener() {

			public void removeUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void insertUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}

			public void changedUpdate(DocumentEvent e) {
				setSaveButtonsEnabled();

			}
		});

		queryField.addKeyListener(new KeyAdapter(){

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyChar() == KeyEvent.VK_DELETE || e.getKeyChar() == KeyEvent.VK_BACK_SPACE){
					deleteSelection();
				}

			}
		});

		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.fill = GridBagConstraints.HORIZONTAL;
		gbc.gridwidth = 3;

		queryPanel.add(queryField,gbc);

		// Quantification Panel
		quantificationPanel = new JPanel(new GridBagLayout());
		quantificationPanel.setBorder(BorderFactory.createTitledBorder("Quantification"));
		quantificationRadioButtonGroup = new ButtonGroup();

		existsDiamond = new JRadioButton("(EF) There exists some reachable marking that satisifies:");
		existsBox = new JRadioButton("(EG) There exists a trace on which every marking satisfies:");
		forAllDiamond = new JRadioButton("(AF) On all traces there is eventually a marking that satisfies:");
		forAllBox = new JRadioButton("(AG) All reachable markings satisfy:");

		quantificationRadioButtonGroup.add(existsDiamond);
		quantificationRadioButtonGroup.add(existsBox);
		quantificationRadioButtonGroup.add(forAllDiamond);
		quantificationRadioButtonGroup.add(forAllBox);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		quantificationPanel.add(existsDiamond, gbc);
		
		// bit of a hack, possible because quantifier node is always the first node atm (we cant have nested quantifiers)
		if (queryToCreateFrom != null){
			if (queryToCreateFrom.getProperty() instanceof TCTLEFNode){
				existsDiamond.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLEGNode){
				existsBox.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLAFNode){
				forAllDiamond.setSelected(true);
			} else if (queryToCreateFrom.getProperty() instanceof TCTLAGNode){
				forAllBox.setSelected(true);
			}
		}

		gbc.gridy = 1;
		quantificationPanel.add(existsBox, gbc);

		gbc.gridy = 2;
		quantificationPanel.add(forAllDiamond, gbc);

		gbc.gridy = 3;
		quantificationPanel.add(forAllBox, gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 1;
		gbc.anchor = GridBagConstraints.WEST;
		queryPanel.add(quantificationPanel,gbc);

		//Add action listeners to the query options
		existsBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEGNode property = new TCTLEGNode(getStateChild(1,currentSelection.getObject()));
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);	
			}
		});

		existsDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLEFNode property = new TCTLEFNode(getStateChild(1,currentSelection.getObject()));

				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
			}
		});

		forAllBox.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAGNode property = new TCTLAGNode(getStateChild(1,currentSelection.getObject()));
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
			}
		});

		forAllDiamond.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				setEnabledReductionOptions();
				TCTLAFNode property = new TCTLAFNode(getStateChild(1,currentSelection.getObject()));
				newProperty = newProperty.replace(currentSelection.getObject(), property);
				updateSelection(property);
			}
		});

		// Logic panel
		logicButtonPanel = new JPanel(new GridBagLayout());
		logicButtonPanel.setBorder(BorderFactory.createTitledBorder("Logic"));

		logicButtonGroup = new ButtonGroup();
		conjunctionButton = new JButton("And");
		disjunctionButton = new JButton("Or");

		logicButtonGroup.add(conjunctionButton);
		logicButtonGroup.add(disjunctionButton);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		logicButtonPanel.add(conjunctionButton,gbc);

		gbc.gridy = 1;
		logicButtonPanel.add(disjunctionButton,gbc);

		gbc = new GridBagConstraints();
		gbc.gridx = 1;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(logicButtonPanel,gbc);

		// Add Action listener for logic buttons
		conjunctionButton.addActionListener(	
				new ActionListener() {
					public void actionPerformed(ActionEvent evt) {
						TCTLAndNode property;
						if(currentSelection.getObject() instanceof TCTLAndNode || currentSelection.getObject() instanceof TCTLOrNode) {
							property = new TCTLAndNode(getStateChild(1,currentSelection.getObject()), 
									getStateChild(2, currentSelection.getObject()));
						}
						else {
							property = new TCTLAndNode(getState(currentSelection.getObject()),getState(currentSelection.getObject()));
						}
						newProperty = newProperty.replace(currentSelection.getObject(), property);
						updateSelection(property);
					}

				}
		);	

		disjunctionButton.addActionListener(
				new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						TCTLOrNode property;
						if(currentSelection.getObject() instanceof TCTLAndNode || currentSelection.getObject() instanceof TCTLOrNode){
							property = new TCTLOrNode(getStateChild(1,currentSelection.getObject()), 
									getStateChild(2, currentSelection.getObject()));
						}
						else {
							property = new TCTLOrNode(getState(currentSelection.getObject()),getState(currentSelection.getObject()));
						}
						newProperty = newProperty.replace(currentSelection.getObject(), property);
						updateSelection(property);

					}
				}
		);

		// Predicate specification panel
		predicatePanel = new JPanel(new GridBagLayout());
		predicatePanel.setBorder(BorderFactory.createTitledBorder("Predicates"));

		String[] places = new String[datalayer.getPlaces().length];
		for (int i=0; i< places.length; i++){
			places[i] = datalayer.getPlaces()[i].getName();
		}

		placesBox = new JComboBox(new DefaultComboBoxModel(places));

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.anchor = GridBagConstraints.WEST;
		predicatePanel.add(placesBox,gbc);

		String[] relationalSymbols= {"=","<=","<",">=",">"};
		relationalOperatorBox = new JComboBox(new DefaultComboBoxModel(relationalSymbols));

		gbc.gridx = 1;
		predicatePanel.add(relationalOperatorBox,gbc);

		int currentValue = 0;
		int min = 0;
		int step = 1;		
		placeMarking = new JSpinner(new SpinnerNumberModel(currentValue, min, Integer.MAX_VALUE, step));
		placeMarking.setMaximumSize(new Dimension(50,30));
		placeMarking.setMinimumSize(new Dimension(50,30));
		placeMarking.setPreferredSize(new Dimension(50,30));

		gbc.gridx = 2;
		predicatePanel.add(placeMarking,gbc);

		addPredicateButton = new JButton("Add Predicate to Query");
		gbc.gridx = 0;
		gbc.gridy = 1;
		predicatePanel.add(addPredicateButton,gbc);

		gbc.gridx = 2;
		gbc.gridy = 1;
		gbc.fill = GridBagConstraints.VERTICAL;
		queryPanel.add(predicatePanel,gbc);

		// Action listeners for predicate panel
		addPredicateButton.addActionListener(
				new ActionListener() {

					public void actionPerformed(ActionEvent e) {
						TCTLAtomicPropositionNode property = new TCTLAtomicPropositionNode((String)placesBox.getSelectedItem(), (String)relationalOperatorBox.getSelectedItem(), (Integer) placeMarking.getValue());
						newProperty = newProperty.replace(currentSelection.getObject(), property);
						updateSelection(property);
					}
				}

		);

		gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 2;
		gbc.anchor = GridBagConstraints.WEST;
		add(queryPanel, gbc);

	}

	private void initUppaalOptionsPanel(final TAPNQuery queryToCreateFrom) {

		uppaalOptionsPanel = new JPanel(new GridBagLayout());

		initSearchOptionsPanel(queryToCreateFrom);
		initTraceOptionsPanel(queryToCreateFrom);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 3;
		add(uppaalOptionsPanel, gridBagConstraints);

	}

	private void initSearchOptionsPanel(final TAPNQuery queryToCreateFrom) {
		//verification option-radio buttons starts here:		
		searchOptionsPanel = new JPanel(new GridBagLayout());

		searchOptionsPanel.setBorder(BorderFactory.createTitledBorder("Analysis Options"));
		searchRadioButtonGroup = new ButtonGroup();
		bFS = new JRadioButton("Breadth First Search");
		dFS = new JRadioButton("Depth First Search");
		rDFS = new JRadioButton("Random Depth First Search");
		closestToTargetFirst = new JRadioButton("Search by Closest To Target First");
		searchRadioButtonGroup.add(bFS);
		searchRadioButtonGroup.add(dFS);
		searchRadioButtonGroup.add(rDFS);
		searchRadioButtonGroup.add(closestToTargetFirst);

		if (queryToCreateFrom==null){
			bFS.setSelected(true);
		}else{
			if (queryToCreateFrom.getSearchOption() == SearchOption.BFS){
				bFS.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.DFS){
				dFS.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.RDFS){
				rDFS.setSelected(true);
			} else if (queryToCreateFrom.getSearchOption() == SearchOption.CLOSE_TO_TARGET_FIRST){
				closestToTargetFirst.setSelected(true);
			}	
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		searchOptionsPanel.add(bFS,gridBagConstraints);
		gridBagConstraints.gridy = 1;
		searchOptionsPanel.add(dFS,gridBagConstraints);
		gridBagConstraints.gridy = 2;
		searchOptionsPanel.add(rDFS,gridBagConstraints);
		gridBagConstraints.gridy = 3;
		searchOptionsPanel.add(closestToTargetFirst,gridBagConstraints);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		uppaalOptionsPanel.add(searchOptionsPanel, gridBagConstraints);

	}

	private void initTraceOptionsPanel(final TAPNQuery queryToCreateFrom) {
		traceOptionsPanel = new JPanel(new GridBagLayout());
		traceOptionsPanel.setBorder(BorderFactory.createTitledBorder("Trace Options"));
		traceRadioButtonGroup = new ButtonGroup();
		some = new JRadioButton("Some encountered trace (only without symmetry reduction)");
		fastest = new JRadioButton("Fastest trace (only without symmetry reduction)");
		none = new JRadioButton("No trace");
		traceRadioButtonGroup.add(some);
		traceRadioButtonGroup.add(fastest);
		traceRadioButtonGroup.add(none);

		if (queryToCreateFrom==null){
			none.setSelected(true);
		}else{
			if (queryToCreateFrom.getTraceOption() == TraceOption.SOME){
				some.setSelected(true);
			} else if (queryToCreateFrom.getTraceOption() == TraceOption.FASTEST){
				fastest.setSelected(true);
			} else if (queryToCreateFrom.getTraceOption() == TraceOption.NONE){
				none.setSelected(true);
			}	
		}

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridy = 0;
		traceOptionsPanel.add(some,gridBagConstraints);
		gridBagConstraints.gridy = 1;
		traceOptionsPanel.add(fastest,gridBagConstraints);
		gridBagConstraints.gridy = 2;
		traceOptionsPanel.add(none,gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		uppaalOptionsPanel.add(traceOptionsPanel, gridBagConstraints);

	}

	private void initReductionOptionsPanel(final TAPNQuery queryToCreateFrom) {


		//ReductionOptions starts here:
		reductionOptionsPanel = new JPanel(new FlowLayout());
		reductionOptionsPanel.setBorder(BorderFactory.createTitledBorder("Reduction Options"));
		String[] reductionOptions = {name_NAIVE, name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2};
		reductionOption = new JComboBox(reductionOptions);
		reductionOption.setSelectedIndex(3);


		reductionOptionsPanel.add(new JLabel("  Choose reduction method:"));
		reductionOptionsPanel.add(reductionOption);

		symmetryReduction = new JCheckBox("Use Symmetry Reduction");
		symmetryReduction.setSelected(true);
		symmetryReduction.addItemListener(new ItemListener(){


			public void itemStateChanged(ItemEvent e) {
				if(e.getStateChange() == ItemEvent.SELECTED){
					disableTraceOptions();
				}else{
					enableTraceOptions();
				}

			}

		});

		reductionOptionsPanel.add(symmetryReduction);
		disableTraceOptions();

		GridBagConstraints gridBagConstraints;
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 4;
		add(reductionOptionsPanel, gridBagConstraints);

		//Update the selected reduction
		if (queryToCreateFrom!=null){
			String reduction = "";
			boolean symmetry = false;

			if(queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST_STANDARD){
				reduction = name_BROADCAST;
				symmetry = false;
				//enableTraceOptions();
			}else if(queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST_SYM){
				reduction = name_BROADCAST;
				symmetry = true;
				//disableTraceOptions();
			}else if(queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST_DEG2){
				reduction = name_BROADCASTDEG2;
				symmetry = false;
				//disableTraceOptions();
			}else if(queryToCreateFrom.getReductionOption() == ReductionOption.BROADCAST_DEG2_SYM){
				reduction = name_BROADCASTDEG2;
				symmetry = true;
				//disableTraceOptions();
			}
			else if (getQuantificationSelection().equals("E<>") || getQuantificationSelection().equals("A[]")){
				if (queryToCreateFrom.getReductionOption() == ReductionOption.NAIVE){
					reduction = name_NAIVE;
					symmetry = false;
					//enableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.NAIVE_UPPAAL_SYM){
					reduction = name_NAIVE;
					symmetry = true;
					//disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.ADV_UPPAAL_SYM){
					reduction = name_ADVNOSYM;
					symmetry = true;
					//disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.ADV_NOSYM){
					reduction = name_ADVNOSYM;
					symmetry = false;
					//enableTraceOptions();
				}
			} else {
				if (queryToCreateFrom.getReductionOption() == ReductionOption.ADV_UPPAAL_SYM){
					reduction = name_ADVNOSYM;
					symmetry = true;
					//disableTraceOptions();
				} else if (queryToCreateFrom.getReductionOption() == ReductionOption.ADV_NOSYM){
					reduction = name_ADVNOSYM;
					symmetry = false;
					//enableTraceOptions();
				}
			}

			reductionOption.setSelectedItem(reduction);
			symmetryReduction.setSelected(symmetry);
		}

	}

	private void initButtonPanel(QueryDialogueOption option, final TAPNQuery queryToCreateFrom) {
		buttonPanel = new JPanel(new FlowLayout());
		if (option == QueryDialogueOption.Save){
			saveButton = new JButton("Save");
			saveAndVerifyButton = new JButton("Save and Verify");
			cancelButton = new JButton("Cancel");
			removeButton = new JButton("Remove");
			saveUppaalXMLButton = new JButton("Save UPPAAL XML");

			saveButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							//TODO make save
							//save();
							querySaved = true;
							exit();
						}
					}
			);
			saveAndVerifyButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							querySaved = true;
							exit();
							Verifier.runUppaalVerification(CreateGui.getModel(), getQuery());
						}
					}
			);
			cancelButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							exit();
						}
					}
			);
			removeButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent evt) {

							datalayer.getQueries().remove(queryToCreateFrom);
							CreateGui.createLeftPane();
							exit();
						}
					}
			);
			saveUppaalXMLButton.addActionListener(
					new ActionListener(){
						public void actionPerformed(ActionEvent e) {
							querySaved = true;

							String xmlFile = null, queryFile = null;
							try {
								xmlFile = new FileBrowser("Uppaal XML","xml",xmlFile).saveFile();
								String[] a = xmlFile.split(".xml");
								queryFile= a[0]+".q";

							} catch (Exception ex) {
								JOptionPane.showMessageDialog(CreateGui.getApp(),
										"There were errors performing the requested action:\n" + e,
										"Error", JOptionPane.ERROR_MESSAGE
								);				
							}

							if(xmlFile != null && queryFile != null){
								Export.exportUppaalXMLFromQuery(CreateGui.getModel(), getQuery(), xmlFile, queryFile);
							}else{
								JOptionPane.showMessageDialog(CreateGui.getApp(), "No Uppaal XML file saved.");
							}
						}
					}
			);
		}else if (option == QueryDialogueOption.Export){
			saveButton = new JButton("export");
			cancelButton = new JButton("Cancel");

			saveButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {
							querySaved = true;
							exit();
						}
					}
			);
			cancelButton.addActionListener(	
					new ActionListener() {
						public void actionPerformed(ActionEvent evt) {

							exit();
						}
					}
			);		
		}
		if (option == QueryDialogueOption.Save){
			buttonPanel.add(cancelButton);

			if (queryToCreateFrom!=null){
				buttonPanel.add(removeButton);	
			}

			buttonPanel.add(saveButton);

			buttonPanel.add(saveAndVerifyButton);

			buttonPanel.add(saveUppaalXMLButton);
		}else {
			buttonPanel.add(cancelButton);

			buttonPanel.add(saveButton);

			//			buttonPanel.add(saveUppaalXMLButton);
		}


		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 5;
		gridBagConstraints.anchor = GridBagConstraints.CENTER;
		add(buttonPanel, gridBagConstraints);

	}

	public TAPNQuery getQuery() {
		if (!querySaved){
			return null;
		}


		String name = getQueryComment();
		int capacity = getCapacity();


		TAPNQuery.TraceOption traceOption = null;
		String traceOptionString = getTraceOptions();

		if (traceOptionString.toLowerCase().contains("some")){
			traceOption = TraceOption.SOME;
		}else if (traceOptionString.toLowerCase().contains("fastest")){
			traceOption = TraceOption.FASTEST;
		}else if (traceOptionString.toLowerCase().contains("no")){
			traceOption = TraceOption.NONE;
		}

		TAPNQuery.SearchOption searchOption = null;
		String searchOptionString = getSearchOptions();

		if (searchOptionString.toLowerCase().contains("breadth")){
			searchOption = SearchOption.BFS;
		}else if (searchOptionString.toLowerCase().contains("depth") && (! searchOptionString.toLowerCase().contains("random")) ){
			searchOption = SearchOption.DFS;
		}else if (searchOptionString.toLowerCase().contains("depth") && searchOptionString.toLowerCase().contains("random") ){
			searchOption = SearchOption.RDFS;
		}else if (searchOptionString.toLowerCase().contains("target")){
			searchOption = SearchOption.CLOSE_TO_TARGET_FIRST;
		}

		TAPNQuery.ReductionOption reductionOptionToSet = null;
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		boolean symmetry = symmetryReduction.isSelected();

		if (reductionOptionString.equals(name_NAIVE) && !symmetry){
			reductionOptionToSet = ReductionOption.NAIVE;
		}else if(reductionOptionString.equals(name_NAIVE) && symmetry){
			reductionOptionToSet = ReductionOption.NAIVE_UPPAAL_SYM;
		}else if (reductionOptionString.equals(name_ADVNOSYM) && !symmetry){
			reductionOptionToSet = ReductionOption.ADV_NOSYM;
		}else if (reductionOptionString.equals(name_ADVNOSYM) && symmetry){
			reductionOptionToSet = ReductionOption.ADV_UPPAAL_SYM;
		}else if(reductionOptionString.equals(name_BROADCAST) && !symmetry){
			reductionOptionToSet = ReductionOption.BROADCAST_STANDARD;
		}else if(reductionOptionString.equals(name_BROADCAST) && symmetry){
			reductionOptionToSet = ReductionOption.BROADCAST_SYM;
		}else if(reductionOptionString.equals(name_BROADCASTDEG2) && !symmetry){
			reductionOptionToSet = ReductionOption.BROADCAST_DEG2;
		}else if(reductionOptionString.equals(name_BROADCASTDEG2) && symmetry){
			reductionOptionToSet = ReductionOption.BROADCAST_DEG2_SYM;
		}


		return new TAPNQuery(name, capacity, newProperty.copy(), traceOption, searchOption, reductionOptionToSet, /*hashTableSizeToSet*/null, /*extrapolationOptionToSet*/ null);
	}

	private int getCapacity(){
		return (Integer) ((JSpinner)boundednessCheckPanel.getComponent(1)).getValue();
	}

	private String getQueryComment() {
		return ((JTextField)namePanel.getComponent(1)).getText();
	}

	private String getTraceOptions() {
		String toReturn = null;
		for (Object radioButton : traceOptionsPanel.getComponents()){
			if( (radioButton instanceof JRadioButton)){
				if ( ((JRadioButton)radioButton).isSelected() ){
					toReturn = ((JRadioButton)radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private String getSearchOptions() {
		String toReturn = null;
		for (Object radioButton : searchOptionsPanel.getComponents()){
			if( (radioButton instanceof JRadioButton)){
				if ( ((JRadioButton)radioButton).isSelected() ){

					toReturn = ((JRadioButton)radioButton).getText();
					break;
				}
			}
		}
		return toReturn;
	}

	private void enableTraceOptions() {
		some.setEnabled(true);
		fastest.setEnabled(true);
	}

	private void disableTraceOptions() {
		some.setEnabled(false);
		fastest.setEnabled(false);
	}

	private void exit(){
		rootPane.getParent().setVisible(false);
	}

	public String getQuantificationSelection() {
		if (existsDiamond.isSelected()){
			return "E<>";
		}else if (existsBox.isSelected()){
			return "E[]";
		}else if (forAllDiamond.isSelected()){
			return "A<>";
		}else if (forAllBox.isSelected()){
			return "A[]";
		} else {
			return "";
		}
	}

	private void disableLivenessReductionOptions(){
		String[] options = null;
		if(!this.datalayer.hasTAPNInhibitorArcs()){
			options = new String[]{name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2};
		}else{
			options = new String[]{name_BROADCAST, name_BROADCASTDEG2};
		}
		
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		boolean selectedOptionStillAvailable = false;
		
		reductionOption.removeAllItems();

		for (String s : options){
			reductionOption.addItem(s);
			if(s.equals(reductionOptionString))
			{
				selectedOptionStillAvailable = true;
			}
		}
		
		if(selectedOptionStillAvailable)
			reductionOption.setSelectedItem(reductionOptionString);
		
	}

	private void enableAllReductionOptions(){
		String reductionOptionString = ""+reductionOption.getSelectedItem();
		reductionOption.removeAllItems();
		if(!this.datalayer.hasTAPNInhibitorArcs()){
			String[] options = {name_NAIVE, name_ADVNOSYM, name_BROADCAST, name_BROADCASTDEG2};

			for (String s : options){
				reductionOption.addItem(s);
			}
		}else {
			//reductionOption.addItem(name_INHIBSTANDARD);
			//reductionOption.addItem(name_INHIBSYM);
			reductionOption.addItem(name_BROADCAST);
			//reductionOption.addItem(name_BROADCASTSYM);
			reductionOption.addItem(name_BROADCASTDEG2);
			//reductionOption.addItem(name_BROADCASTDEG2SYM);
			//reductionOption.addItem(name_ADVBROADCASTSYM);
			//reductionOption.addItem(name_OPTBROADCAST);
			//reductionOption.addItem(name_OPTBROADCASTSYM);
			//reductionOption.addItem(name_SUPERBROADCAST);
			//reductionOption.addItem(name_SUPERBROADCASTSYM);
		}
		
		reductionOption.setSelectedItem(reductionOptionString);
		
		
	}



	public static TAPNQuery ShowUppaalQueryDialogue(QueryDialogueOption option, TAPNQuery queryToRepresent){
		EscapableDialog guiDialog = 
			new EscapableDialog(CreateGui.getApp(), Pipe.getProgramName(), true);

		Container contentPane = guiDialog.getContentPane();

		// 1 Set layout
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.PAGE_AXIS));      

		// 2 Add query editor
		QueryDialogue queryDialogue = new QueryDialogue(guiDialog, CreateGui.getModel(), option, queryToRepresent); 
		contentPane.add( queryDialogue );

		guiDialog.setResizable(false);     

		// Make window fit contents' preferred size
		guiDialog.pack();

		// Move window to the middle of the screen
		guiDialog.setLocationRelativeTo(null);
		guiDialog.setVisible(true);

		return  queryDialogue.getQuery();
	}

	private void setEnabledReductionOptions() {
		if (getQuantificationSelection().equals("E[]") || getQuantificationSelection().equals("A<>")) {
			disableLivenessReductionOptions();
		} else {
			enableAllReductionOptions();
		}

	}

	private void disableAllQueryButtons() {
		existsBox.setEnabled(false);
		existsDiamond.setEnabled(false);
		forAllBox.setEnabled(false);
		forAllDiamond.setEnabled(false);
		conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		placesBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);

	}

	private void enablePathButtons() {
		existsBox.setEnabled(true);
		existsDiamond.setEnabled(true);
		forAllBox.setEnabled(true);
		forAllDiamond.setEnabled(true);
		conjunctionButton.setEnabled(false);
		disjunctionButton.setEnabled(false);
		placesBox.setEnabled(false);
		relationalOperatorBox.setEnabled(false);
		placeMarking.setEnabled(false);
		addPredicateButton.setEnabled(false);
	}

	private void enableStateButtons() {
		existsBox.setEnabled(false);
		existsDiamond.setEnabled(false);
		forAllBox.setEnabled(false);
		forAllDiamond.setEnabled(false);
		conjunctionButton.setEnabled(true);
		disjunctionButton.setEnabled(true);
		placesBox.setEnabled(true);
		relationalOperatorBox.setEnabled(true);
		placeMarking.setEnabled(true);
		addPredicateButton.setEnabled(true);

	}


	private TCTLAbstractStateProperty getState(TCTLAbstractProperty property) {
		if (property instanceof TCTLAbstractStateProperty) {
			return (TCTLAbstractStateProperty)property.copy();
		} else {
			return new TCTLStatePlaceHolder();
		}
	}

	private TCTLAbstractStateProperty getStateChild(int number, TCTLAbstractProperty property) {
		StringPosition[] children = property.getChildren();
		int count = 0;
		for (int i = 0; i < children.length; i++) {
			TCTLAbstractProperty child = children[i].getObject();
			if (child instanceof TCTLAbstractStateProperty) {
				count++;
				if (count == number) {
					return (TCTLAbstractStateProperty)child;
				}
			}
		}
		return new TCTLStatePlaceHolder();
	}

	private void updateSelection() {
		int index = queryField.getCaretPosition();
		StringPosition position = newProperty.objectAt(index);
		if (position == null) return;
		queryField.select(position.getStart(),position.getEnd());
		currentSelection = position;
		if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
			enableStateButtons();
		} else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
			enablePathButtons();
		} else {
			disableAllQueryButtons();
		}

	}


	private void updateSelection(TCTLAbstractProperty newSelection) {
		queryField.setText(newProperty.toString());
		if (currentSelection != null) {
			StringPosition position = newProperty.indexOf(newSelection);
			queryField.select(position.getStart(),position.getEnd());
			currentSelection = position;
			if (currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
				enableStateButtons();
			} else if (currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
				enablePathButtons();
			} else {
				disableAllQueryButtons();
			}

		} else {
			clearSelection();
		}
	}

	private void deleteSelection() {
		TCTLAbstractProperty replacement = null;
		if(currentSelection.getObject() instanceof TCTLAbstractStateProperty) {
			replacement = getStateChild(1, currentSelection.getObject());
		} else if(currentSelection.getObject() instanceof TCTLAbstractPathProperty) {
			replacement = new TCTLPathPlaceHolder();
		}
		if(replacement !=null) {
			newProperty = newProperty.replace(currentSelection.getObject(), replacement);
			updateSelection(replacement);
		}
	}

	private void clearSelection() {
		queryField.selectAll();
		queryField.select(0, 0);
		currentSelection = null;
		disableAllQueryButtons();
	}

	private void setSaveButtonsEnabled(){
		boolean isQueryOk = getQueryComment().length() > 0 && !newProperty.containsPlaceHolder();
		saveButton.setEnabled(isQueryOk);
		saveAndVerifyButton.setEnabled(isQueryOk);
		saveUppaalXMLButton.setEnabled(isQueryOk);
	}

}