package org.dawnsci.january.ui.dataconfigtable;

import org.dawnsci.january.model.ISliceAssist;
import org.dawnsci.january.model.NDimensions;
import org.eclipse.january.dataset.Slice;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SliceEditingSupport extends EditingSupport {
	
	private static final Logger logger = LoggerFactory.getLogger(SliceEditingSupport.class);

	private TextCellEditor editor;
	private Shell sliderShell;
	private Slider slider;
	private int currentDimension = -1;
	private boolean sliding = false;
	private int maxSliceSize = 50;
	
	public SliceEditingSupport(ColumnViewer viewer) {
		super(viewer);
		editor = new TextCellEditor((Composite) getViewer().getControl(), SWT.NONE);
		Control control = editor.getControl();
		editor.getControl().addListener(SWT.Activate, new Listener() {
			
			@Override
			public void handleEvent(Event event) {
				setShowingSlider(true);
				
			}
		});
		
		if (control instanceof Text) {
			Text t = (Text)control;
			
			t.addVerifyListener(new VerifyListener() {
				
				@Override
				public void verifyText(VerifyEvent e) {
					
					 //Validation for keys like Backspace, left arrow key, right arrow key and del keys
					if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT
							|| e.keyCode == SWT.ARROW_RIGHT
							|| e.keyCode == SWT.DEL || e.character == ':') {
						e.doit = true;
						return;
					}

					if (e.character == '\0') {
						e.doit = true;
						return;
					}
					
					if (e.character == '-') {
						e.doit = true;
						return;
					}
					
					if (!('0' <= e.character && e.character <= '9')){
						e.doit = false;
						return;
					}
				}
			});
		}
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		currentDimension = (int) element;
		((Text)editor.getControl()).setText("25");
		((Text)editor.getControl()).setSelection(26);
		if (sliderShell!=null&&sliderShell.isVisible()) return editor;
		return editor;
	}
	
	public boolean isActive() {
		return editor.isActivated();
	}

	@Override
	protected boolean canEdit(Object element) {
		return true;
	}

	@Override
	protected Object getValue(Object element) {
		
		currentDimension = (int)element;
	
		return getSlice(currentDimension).toString();
	}
	
	private int[] getSliderSelectionThumb(Slice slice, int size) {
		int start = slice.getStart() == null ? 0 : slice.getStart();
		int stop = slice.getStop() == null ? size : slice.getStop();
		
		int thumb = (stop - start);
		
		return new int[]{start, thumb};
	}
	
	private Slice getSlice(int i) {
		return ((NDimensions)getViewer().getInput()).getSlice(i);
	}
	
	private void setSlice(int i, Slice slice) {
		NDimensions ndims = ((NDimensions)getViewer().getInput());
		if (slice.getStop() == null) {
			slice.setStop(ndims.getSize(i));
		}
		
		if (ndims.getDescription(i) == null || ndims.getDescription(i).isEmpty()){
			if (slice.getStart() > ndims.getSize(i)-1) slice.setStart(0);
			int numSteps = slice.getNumSteps();
			if (numSteps > maxSliceSize) {
				slice.setStop(slice.getStart()+(maxSliceSize*slice.getStep()));
			}
		} else {
			int size = ndims.getSize(i);
			if (slice.getStart() > size || slice.getStop() > size){
				slice.setStart(0);
				slice.setStop(size-1);
			}
		}
		ndims.setSlice(i, slice);
	}
	
	private int getSize(int i) {
		return ((NDimensions)getViewer().getInput()).getSize(i);
	}


	@Override
	protected void setValue(Object element, Object value) {
		if (value.toString().isEmpty())return;
		try {
			Slice[] s = Slice.convertFromString(value.toString());
			if (s == null) {
				s = new Slice[]{getSlice(currentDimension)};
			}else {
				setSlice(currentDimension,s[0]);
			}
			if (slider != null && slider.getVisible()) {
				int size = getSize(currentDimension);
				Slice slice = getSlice((int)element);
				updateSlider(slice,size);
			}

			getViewer().refresh();
		} catch (Exception e) {
			logger.error("Could not set slice",e);
		}

	}
	
	private void updateSlider(Slice slice, int size) {
		slider.setMinimum(0);
		int[] sss = getSliderSelectionThumb(slice, size);
		slider.setMaximum(size);
		slider.setSelection(sss[0]);
		slider.setThumb(sss[1]);
		slider.setIncrement(1);
		slider.setPageIncrement(10);
	}
	
	private void createSliderShell() {
		if (sliderShell != null) return;
		
		sliderShell = new Shell(this.getViewer().getControl().getShell(), SWT.ON_TOP | SWT.TOOL);
		sliderShell.setLayout(new GridLayout(1, false));
		
		final Listener closeListener = new Listener() {
			@Override
			public void handleEvent(final Event e) {
				if (e.type == SWT.Traverse) setShowingSlider(false);
				if (sliding) return;
				if (slider.isFocusControl()) return;
				setShowingSlider(false);
			}
		};

		// Listeners on this popup's shell
		sliderShell.addListener(SWT.Deactivate, closeListener);
		sliderShell.addListener(SWT.Close, closeListener);
		sliderShell.addListener(SWT.FocusOut, closeListener);

		// Listeners on the target control
		editor.getControl().addListener(SWT.MouseDoubleClick, closeListener);
		editor.getControl().addListener(SWT.MouseDown, closeListener);
		editor.getControl().addListener(SWT.Dispose, closeListener);
		//focus out so when editor loses focus the slider vanishes
		//sliding boolean stops it vanishing when sliding
		editor.getControl().addListener(SWT.FocusOut, closeListener);
		editor.getControl().addListener(SWT.Traverse, closeListener);
		// Listeners on the target control's shell
		Shell controlShell = editor.getControl().getShell();
		controlShell.addListener(SWT.Move, closeListener);
		controlShell.addListener(SWT.Resize, closeListener);


        slider = new Slider(sliderShell, SWT.HORIZONTAL);
        //we want the slider to have the stepper buttons
        slider.setData("org.eclipse.swt.internal.gtk.css", "scrollbar {-GtkScrollbar-has-backward-stepper: true; -GtkScrollbar-has-forward-stepper: true;}");
        slider.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        slider.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseUp(MouseEvent e) {
				sliding = false;
				
			}
			
			@Override
			public void mouseDown(MouseEvent e) {
				sliding = true;
				slider.getShell().setVisible(true);
				
			}
		});
        
        slider.addMouseTrackListener(new MouseTrackAdapter() {
			
			@Override
			public void mouseExit(MouseEvent e) {
				sliding = false;
				
			}
			
			@Override
			public void mouseEnter(MouseEvent e) {
				sliding = true;
				
			}
		});

		slider.addSelectionListener(new SelectionAdapter() {
			private int last = slider.getSelection();

			public void widgetSelected(SelectionEvent e) {
				slider.setFocus();
				int current = slider.getSelection();
				if (current != last) {
					updateSlice(current);
					last = current;
				}
			}
		});

		slider.addFocusListener(new FocusListener() {
			@Override
			public void focusGained(FocusEvent e) { }
			@Override
			public void focusLost(FocusEvent e) {
				setShowingSlider(false);
			}
        });
        
        slider.setIncrement(1);
        
        sliderShell.pack();
	}
	
	private void updateSlice(int i) {
		
		Slice slice = getSlice(currentDimension);
		int size = getSize(currentDimension);
		
		int[] sst = getSliderSelectionThumb(slice,size);
		
		int step = slice.getStep();
		
		Slice s = new Slice(i, i+sst[1], step);
		setSlice(currentDimension,s);
		editor.setValue(s.toString());
		getViewer().refresh();
	}
	
	protected void setShowingSlider(final boolean isShow) {
		if (isShow) {
			createSliderShell();
			updateSlider(getSlice(currentDimension), getSize(currentDimension));
			Rectangle eb= editor.getControl().getBounds();
			Point     ep = editor.getControl().toDisplay(0, eb.height);
			
			ColumnViewer v = getViewer();
			
			int x = ep.x;
			int y = ep.y;
			int width = eb.width;
			
			if (v instanceof TableViewer) {
				Table table = ((TableViewer) v).getTable();
				Rectangle tb = table.getBounds();
				width = tb.width;
				Point     tp = table.toDisplay(0, eb.height);
				x = tp.x;
			}
			
			Rectangle rect = new Rectangle(x,y, width, sliderShell.getBounds().height);
			sliderShell.setBounds(rect);
			sliderShell.setVisible(true);
		} else {
			if (sliderShell!=null&&!sliderShell.isDisposed()) {
				sliderShell.setVisible(false);
			}
		}
	}

	public int getMaxSliceSize() {
		return maxSliceSize;
	}

	public void setMaxSliceSize(int maxSliceSize) {
		this.maxSliceSize = maxSliceSize;
	}
	
	public void setSliceAssist(ISliceAssist sliceAssist) {
		Menu menu = new Menu(editor.getControl());
		MenuItem item = new MenuItem(menu, SWT.PUSH);
		item.setText(sliceAssist.getLabel());
		item.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				NDimensions nd =(NDimensions)getViewer().getInput();
				Slice slice = sliceAssist.getSlice(nd, currentDimension);
				if (slice != null) {
					setSlice(currentDimension, slice);
					getViewer().refresh();
				}
			}

		});
		editor.getControl().setMenu(menu);
	}
}

	

