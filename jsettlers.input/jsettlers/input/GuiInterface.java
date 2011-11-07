package jsettlers.input;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.map.shapes.MapCircle;
import jsettlers.common.map.shapes.MapShapeFilter;
import jsettlers.common.movable.IMovable;
import jsettlers.common.position.ILocatable;
import jsettlers.common.position.ISPosition2D;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.common.selectable.ISelectable;
import jsettlers.graphics.action.Action;
import jsettlers.graphics.action.BuildAction;
import jsettlers.graphics.action.EActionType;
import jsettlers.graphics.action.MoveToAction;
import jsettlers.graphics.action.ScreenChangeAction;
import jsettlers.graphics.action.SelectAction;
import jsettlers.graphics.action.SelectAreaAction;
import jsettlers.graphics.map.IMapInterfaceListener;
import jsettlers.graphics.map.MapInterfaceConnector;
import jsettlers.graphics.map.selection.BuildingSelection;
import jsettlers.graphics.map.selection.EmptySelection;
import jsettlers.graphics.map.selection.ISelectionSet;
import jsettlers.graphics.map.selection.SettlerSelection;
import jsettlers.input.task.GeneralGuiTask;
import jsettlers.input.task.MoveToGuiTask;
import jsettlers.input.task.SimpleGuiTask;
import jsettlers.input.task.TaskExecutor;
import jsettlers.input.task.WorkAreaGuiTask;
import jsettlers.logic.buildings.Building;
import jsettlers.logic.map.newGrid.interfaces.IHexMovable;
import jsettlers.logic.movable.IDebugable;
import jsettlers.logic.movable.IIDable;
import network.INetworkManager;
import synchronic.timer.NetworkTimer;

/**
 * class to handle the events provided by the user through jsettlers.graphics
 * 
 * @author Andreas Eberle
 */
public class GuiInterface implements IMapInterfaceListener {

	private ISelectionSet currentSelection = new EmptySelection();
	private final MapInterfaceConnector connector;

	/**
	 * The current active action that waits for the user to select a point.
	 */
	private Action activeAction = null;
	private final INetworkManager manager;
	private final IGuiInputGrid grid;
	private EBuildingType previewBuilding;

	public GuiInterface(MapInterfaceConnector connector, INetworkManager manager, IGuiInputGrid grid) {
		this.connector = connector;
		this.manager = manager;
		this.grid = grid;
		TaskExecutor.init(grid);
		connector.addListener(this);
	}

	@Override
	public void action(Action action) {
		if (action.getActionType() != EActionType.SCREEN_CHANGE) {
			System.out.println("action(Action): " + action.getActionType());
		}

		switch (action.getActionType()) {
		case BUILD:
			EBuildingType buildingType = ((BuildAction) action).getBuilding();
			System.err.println("build: " + buildingType);
			this.previewBuilding = buildingType; // FIXME implement a way to give graphics grid the preview building
			grid.setBuildingType(buildingType);
			setActiveAction(action);
			break;

		case DEBUG_ACTION:
			for (ISelectable curr : currentSelection) {
				if (curr instanceof IDebugable) {
					((IDebugable) curr).debug();
				}
			}
			break;

		case SPEED_TOGGLE_PAUSE:
			NetworkTimer.get().invertPausing();
			break;

		case SPEED_SLOW:
			if (!manager.isMultiplayer())
				NetworkTimer.setGameSpeed(0.5f);
			break;
		case SPEED_FAST:
			if (!manager.isMultiplayer())
				NetworkTimer.setGameSpeed(2.0f);
			break;
		case SPEED_FASTER:
			if (!manager.isMultiplayer())
				NetworkTimer.multiplyGameSpeed(1.2f);
			break;
		case SPEED_SLOWER:
			if (!manager.isMultiplayer())
				NetworkTimer.multiplyGameSpeed(1 / 1.2f);
			break;
		case SPEED_NORMAL:
			if (!manager.isMultiplayer())
				NetworkTimer.setGameSpeed(1.0f);
			break;

		case SELECT_POINT:
			handleSelectPointAction((SelectAction) action);
			break;

		case SELECT_AREA:
			selectArea((SelectAreaAction) action);
			break;

		case MOVE_TO:
			MoveToAction moveToAction = (MoveToAction) action;
			ISPosition2D pos = moveToAction.getPosition();

			moveTo(pos);
			break;

		case FAST_FORWARD:
			if (!manager.isMultiplayer()) {
				NetworkTimer.get().fastForward();
			}
			break;

		case SET_WORK_AREA:
			setActiveAction(action);
			break;

		case DESTROY:
			// TODO handle destroy actions of buildings
			break;

		case STOP_WORKING:
			stopOrStartWorkingAction(true);
			break;
		case START_WORKING:
			stopOrStartWorkingAction(false);
			break;

		case SHOW_SELECTION:
			showSelection();
			break;

		case SCREEN_CHANGE:
			grid.setScreen(((ScreenChangeAction) action).getScreenArea());
			break;

		case TOGGLE_DEBUG:
			// grid.resetDebugColors();
			break;

		default:
			System.err.println("GuiInterface.action() called, but event can't be handled... (" + action.getActionType() + ")");
		}
	}

	private void showSelection() {
		int x = 0;
		int y = 0;
		int count = 0;
		for (ISelectable member : currentSelection) {
			if (member instanceof ILocatable) {
				x += ((ILocatable) member).getPos().getX();
				y += ((ILocatable) member).getPos().getY();
				count++;
			}
		}
		System.out.println("locatable: " + count);
		if (count > 0) {
			ISPosition2D point = new ShortPoint2D(x / count, y / count);
			connector.scrollTo(point, false);
		}
	}

	/**
	 * @param stop
	 *            if true the members of currentSelection will stop working<br>
	 *            if false, they will start working
	 */
	private void stopOrStartWorkingAction(boolean stop) {
		for (ISelectable curr : currentSelection) {
			curr.stopOrStartWorking(stop);
		}
	}

	private void moveTo(ISPosition2D pos) {
		List<Integer> selectedIds = new LinkedList<Integer>();

		for (ISelectable curr : currentSelection) {
			if (curr instanceof IIDable) {
				selectedIds.add(((IIDable) curr).getID());
			}
		}

		scheduleTask(new MoveToGuiTask(EGuiAction.MOVE_TO, pos, selectedIds));
	}

	private void setActiveAction(Action action) {
		if (this.activeAction != null) {
			// TODO: if it was a build action, remove build images
			this.activeAction.setActive(false);
		}
		this.activeAction = action;
		if (action != null) {
			action.setActive(true);
		}
	}

	private void selectArea(SelectAreaAction action) {
		ArrayList<IMovable> foundMovables = new ArrayList<IMovable>();
		IBuilding foundBuilding = null;

		for (ISPosition2D curr : new MapShapeFilter(action.getArea(), grid.getWidth(), grid.getHeight())) {
			IMovable movable = grid.getMovable(curr.getX(), curr.getY());
			if (movable != null) {
				foundMovables.add(movable);
			}
			IBuilding building = grid.getBuildingAt(curr.getX(), curr.getY());
			if (building != null) {
				foundBuilding = building;
			}
		}

		if (!foundMovables.isEmpty()) {
			setSelection(new SettlerSelection(foundMovables));
		} else if (foundBuilding != null) {
			setSelection(new BuildingSelection(foundBuilding));
		} else {
			setSelection(new EmptySelection());
		}
	}

	private void handleSelectPointAction(SelectAction action) {
		SelectAction selectAction = action;
		ISPosition2D pos = selectAction.getPosition();
		System.out.println("clicked: ( " + pos.getX() + " | " + pos.getY() + " )");

		if (activeAction == null) {
			select(pos);
		} else {
			switch (activeAction.getActionType()) {
			case BUILD:
				EBuildingType type = previewBuilding;
				if (grid.canConstructAt(pos, type)) {
					previewBuilding = null;
					grid.setBuildingType(null);
					scheduleTask(new GeneralGuiTask(EGuiAction.BUILD, pos, type));
					break;
				} else {
					return; // prevent resetting the current action
				}
			case SET_WORK_AREA:
				if (currentSelection.getSize() > 0) {
					ISelectable selected = currentSelection.iterator().next();
					if (selected instanceof Building) {
						scheduleTask(new WorkAreaGuiTask(EGuiAction.SET_WORK_AREA, pos, ((Building) selected).getPos()));
					}
				}
				break;
			}

			setActiveAction(null);
		}
	}

	private void scheduleTask(SimpleGuiTask guiTask) {
		manager.scheduleTask(guiTask);
	}

	private void select(ISPosition2D pos) {
		if (grid.isInBounds(pos)) {
			short x = pos.getX();
			short y = pos.getY();

			IHexMovable m1 = grid.getMovable((short) (x + 1), (short) (y + 1));
			IHexMovable m2 = grid.getMovable((x), (short) (y + 1));
			IHexMovable m3 = grid.getMovable(x, y);
			IHexMovable m4 = grid.getMovable((short) (x + 1), (short) (y + 2));

			if (m1 != null) {
				setSelection(new SettlerSelection(Collections.singletonList(m1)));
			} else if (m2 != null) {
				setSelection(new SettlerSelection(Collections.singletonList(m2)));
			} else if (m3 != null) {
				setSelection(new SettlerSelection(Collections.singletonList(m3)));
			} else if (m4 != null) {
				setSelection(new SettlerSelection(Collections.singletonList(m4)));

			} else {
				// search buildings
				IBuilding building = getBuildingAround(pos);
				if (building != null) {
					setSelection(new BuildingSelection(building));
				} else {
					setSelection(new EmptySelection());
				}
			}
		}
	}

	private IBuilding getBuildingAround(ISPosition2D pos) {
		for (ISPosition2D curr : new MapCircle(pos.getX(), pos.getY(), 5)) {
			if (grid.isInBounds(curr)) {
				IBuilding building = grid.getBuildingAt(curr.getX(), curr.getY());
				if (building != null) {
					return building;
				}
			}
		}
		return null;
	}

	/**
	 * Sets the selection.
	 * 
	 * @param selection
	 *            The selected items. Not null!
	 */
	private void setSelection(ISelectionSet selection) {
		for (ISelectable unselected : this.currentSelection) {
			unselected.setSelected(false);
			if (unselected instanceof Building) {
				((Building) unselected).drawWorkAreaCircle(false);
			}
		}
		for (ISelectable selected : selection) {
			selected.setSelected(true);
			if (selected instanceof Building) {
				((Building) selected).drawWorkAreaCircle(true);
			}
		}

		this.connector.setSelection(selection);
		this.currentSelection = selection;
	}

}
