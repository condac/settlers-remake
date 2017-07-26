/*******************************************************************************
 * Copyright (c) 2017
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"),
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense,
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
 * DEALINGS IN THE SOFTWARE.
 *******************************************************************************/
package jsettlers.logic.buildings.workers;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import jsettlers.common.buildings.EBuildingType;
import jsettlers.common.buildings.IBuilding;
import jsettlers.common.buildings.stacks.RelativeStack;
import jsettlers.common.material.EMaterialType;
import jsettlers.common.movable.EDirection;
import jsettlers.common.movable.EMovableType;
import jsettlers.common.position.ShortPoint2D;
import jsettlers.logic.DockPosition;
import jsettlers.logic.buildings.IBuildingsGrid;
import jsettlers.logic.buildings.stack.IRequestStack;
import jsettlers.logic.buildings.stack.RequestStack;
import jsettlers.logic.movable.Movable;
import jsettlers.logic.player.Player;

/**
 * An extension to the worker building for dockyards
 */
public class DockyardBuilding extends WorkerBuilding implements IBuilding.IShipConstruction {
	private static final long serialVersionUID = -6262522980943839741L;

	private enum EShipType {
		FERRY(EMovableType.FERRY, 4, 1, 30),
		CARGO_SHIP(EMovableType.CARGO_BOAT, 6, 1, 42);

		public final EMovableType movableType;
		public final short requiredPlanks;
		public final short requiredIron;
		public final int buildingSteps;

		EShipType(EMovableType movableType, int requiredPlanks, int requiredIron, int buildingSteps) {
			this.movableType = movableType;
			this.requiredPlanks = (short) requiredPlanks;
			this.requiredIron = (short) requiredIron;
			this.buildingSteps = buildingSteps;
		}

		public EShipType get(EMovableType movableType) {
			switch (movableType) {
			case FERRY:
				return FERRY;
			case CARGO_BOAT:
				return CARGO_SHIP;
			default:
				throw new IllegalArgumentException("MovableType is no ship: " + movableType);
			}
		}

		public short getRequiredMaterial(EMaterialType materialType) {
			switch (materialType) {
			case PLANK:
				return requiredPlanks;
			case IRON:
				return requiredIron;
			default:
				return 0;
			}
		}
	}

	private EShipType orderedShipType = null;
	private Movable ship = null;
	private DockPosition dockPosition = null;

	public DockyardBuilding(Player player, ShortPoint2D position, IBuildingsGrid buildingsGrid) {
		super(EBuildingType.DOCKYARD, player, position, buildingsGrid);
	}

	protected List<? extends IRequestStack> createWorkStacks() {
		if (orderedShipType == null) {
			return Collections.emptyList();
		}

		List<RequestStack> newStacks = new LinkedList<>();

		for (RelativeStack stack : type.getRequestStacks()) {
			short requiredAmount = orderedShipType.getRequiredMaterial(stack.getMaterialType());
			if (requiredAmount > 0) {
				newStacks.add(new RequestStack(grid.getRequestStackGrid(), stack.calculatePoint(this.pos), stack.getMaterialType(), type, getPriority(), requiredAmount));
			}
		}

		return newStacks;
	}

	public void buildShipAction() {
		if (this.ship == null) {
			ShortPoint2D position = this.dockPosition.getDirection().getNextHexPoint(this.dockPosition.getPosition(), 5);
			// push old ship
			this.ship = (Movable) super.grid.getMovableGrid().getMovableAt(position.x, position.y);
			if (this.ship != null) {
				this.ship.leavePosition();
			}
			// make new ship
			this.ship = new Movable(super.grid.getMovableGrid(), this.orderedShipType.movableType, position, super.getPlayer());
			EDirection direction = dockPosition.getDirection().rotateRight(1);
			this.ship.setDirection(direction);
		}

		this.ship.increaseStateProgress((float) (1. / orderedShipType.buildingSteps));

		if (this.ship.getStateProgress() >= .99) {
			this.ship = null;
			this.orderedShipType = null;
		}
	}

	public boolean setDock(DockPosition dockPosition) {
		if (this.dockPosition != null) { // replace dock
			if (this.ship != null) {
				return false; // do not change the dock when a ship is in construction
			}
			this.grid.setDock(this.dockPosition, false, this.getPlayer());
		}
		this.dockPosition = dockPosition;
		this.grid.setDock(dockPosition, true, this.getPlayer());
		return true;
	}

	public DockPosition getDock() {
		return this.dockPosition;
	}

	public void removeDock() {
		if (this.dockPosition == null) {
			return;
		}
		this.grid.setDock(this.dockPosition, false, this.getPlayer());
		this.dockPosition = null;
	}

	public void orderFerry() {
		setOrder(EShipType.FERRY);
	}

	public void orderCargoBoat() {
		setOrder(EShipType.CARGO_SHIP);
	}

	private void setOrder(EShipType shipType) {
		if (orderedShipType != null) {
			return;
		}

		this.orderedShipType = shipType;
		initWorkStacks();
	}

	@Override
	public EMovableType getOrderedShipType() { // TODO use EShipType outside of this class
		return orderedShipType == null ? null : orderedShipType.movableType;
	}
}
