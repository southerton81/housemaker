package com.kurovsky.houseoftheday;

import java.util.Random;

import com.kurovsky.houseoftheday.ScoreAccumulator.StageEnum;

public class ShapesGenerator {
	public static final int WALL_SHAPES_COUNT = 15;
	public static final int ROOF_SHAPES_COUNT = 7;

	private Random mRandomNumberGenerator = new Random();
	private int    mChancesForRoof = 11;	
	private int    mWallType;
	private int    mContinuousRoofs = 0;

	void GenerateShape(Grid Piece, int PieceNum, ScoreAccumulator.StageEnum stage) {
		Piece.Clear();
		boolean CanGenerateRoofs = (PieceNum > 3);
		
		int isRoof = mRandomNumberGenerator.nextInt(mChancesForRoof);
		
		if (CanGenerateRoofs && (isRoof < 4) && (mContinuousRoofs < 4)){
			GenerateRoofShape(Piece);
			mContinuousRoofs++;
		}
		else{
			GenerateWallShape(Piece, stage);
			mContinuousRoofs = 0;
			Piece.Rotate(mRandomNumberGenerator.nextInt(2));
		}
		
		Piece.SetFigureUid(PieceNum);
	}

	void GenerateWallShape(Grid Piece, ScoreAccumulator.StageEnum stage) {	
		int PossibleWallTypesCount = (int) stage.PieceCount();

		if (PossibleWallTypesCount > Game.WALL_TYPES_COUNT) 
			PossibleWallTypesCount = Game.WALL_TYPES_COUNT;
		
		mWallType = mRandomNumberGenerator.nextInt(PossibleWallTypesCount);
		int WallShape = mRandomNumberGenerator.nextInt(WALL_SHAPES_COUNT);
		
		switch (WallShape) {
		case 0: case 1: MakeLShape(Piece); break;
		case 2: case 3: MakeDoubleShape(Piece); break;
		case 4: case 5: MakeGShape(Piece); break;
		case 6: case 7: MakeNoseShape(Piece); break;
		case 8: case 9: MakeTripleShape(Piece); break;
		case 10: case 11: MakeQuadShape(Piece); break;
		case 12: case 13: MakeLadderShape(Piece); break;
		case 14: default: MakeSingleShape(Piece); break;
		}
	}
	
	void GenerateRoofShape(Grid Piece) {
		int RoofShape = mRandomNumberGenerator.nextInt(ROOF_SHAPES_COUNT);
		
		switch (RoofShape) {
		case 0: MakeDoubleRoofShape(Piece); break;
		case 1: MakeDoubleRoofShape(Piece); break;
		case 2: MakeSingleRoofShape(Piece); break;
		case 3: MakeSingleRoofShape(Piece); break;
		case 4: MakeSingleRoofShape(Piece); break;
		case 5: MakeTrippleRoofShape(Piece); break;
		case 6: MakeLadderRoofShape(Piece); break;
		}
	}
	
	void MakeDoubleRoofShape(Grid Piece){
		Piece.SetSize(2, 2);
		int Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(0, 0, Grid.ROOF_TYPE, Style);
		Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(1, 0, Grid.ROOF_TYPE, Style);
		Piece.SetNumRotations(2);
	}
	
	void MakeSingleRoofShape(Grid Piece){
		Piece.SetSize(1, 1);
		int Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(0, 0, Grid.ROOF_TYPE, Style);
		Piece.SetNumRotations(0);
	}
	
	void MakeLadderRoofShape(Grid Piece){
		Piece.SetSize(2, 2);
		int Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(0, 0, Grid.ROOF_TYPE, Style);
		Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(1, 1, Grid.ROOF_TYPE, Style);
		Piece.SetNumRotations(2);
	}
	
	void MakeTrippleRoofShape(Grid Piece){
		Piece.SetSize(3, 3);
		int Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(0, 0, Grid.ROOF_TYPE, Style);
		Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(1, 0, Grid.ROOF_TYPE, Style);
		Style = mRandomNumberGenerator.nextInt(Game.ROOF_STYLES_COUNT);
		Piece.SetTile(2, 0, Grid.ROOF_TYPE, Style);
		Piece.SetNumRotations(2);
	}

	void MakeGShape(Grid Piece){	
		Piece.SetSize(2, 2);
		int Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 0, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(1, 0, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 1, mWallType, Style);		
		Piece.SetNumRotations(4);
	}
	
	void MakeDoubleShape(Grid Piece){
		Piece.SetSize(2, 2);
		int Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 0, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 1, mWallType, Style);	
		Piece.SetNumRotations(2);
	}
	
	void MakeTripleShape(Grid Piece){
		Piece.SetSize(3, 3);
		int Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 0, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 1, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 2, mWallType, Style);
		Piece.SetNumRotations(2);
	}
	
	void MakeLShape(Grid Piece){
		Piece.SetSize(3, 3);
		int Style = 0;
		
		boolean Invert = mRandomNumberGenerator.nextBoolean();
		
		if (Invert) {
			mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 0, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 1, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 2, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(0, 2, mWallType, Style);
		}
		else {
			mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(0, 0, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(0, 1, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(0, 2, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 2, mWallType, Style);
		}
		
		Piece.SetNumRotations(4);
	}
	
	void MakeNoseShape(Grid Piece){
		Piece.SetSize(3, 3);
		int Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 0, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 1, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 2, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(1, 1, mWallType, Style);
		Piece.SetNumRotations(4);
	}
	
	void MakeQuadShape(Grid Piece){
		Piece.SetSize(2, 2);
		int Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 0, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 1, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(1, 1, mWallType, Style);
		Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(1, 0, mWallType, Style);
		Piece.SetNumRotations(4);
	}
	
	void MakeLadderShape(Grid Piece){
		Piece.SetSize(3, 3);
		int Style = 0;

		boolean Invert = mRandomNumberGenerator.nextBoolean();

		if (Invert) {
			mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(2, 0, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(2, 1, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 1, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 2, mWallType, Style);
			Piece.SetNumRotations(4);
		}
		else {
			mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(0, 0, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(0, 1, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 1, mWallType, Style);
			Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
			Piece.SetTile(1, 2, mWallType, Style);
			Piece.SetNumRotations(4);
		}
	}
	
	void MakeSingleShape(Grid Piece){
		Piece.SetSize(1, 1);
		int Style = mRandomNumberGenerator.nextInt(Game.WALL_STYLES_COUNT);
		Piece.SetTile(0, 0, mWallType, Style);
		Piece.SetNumRotations(0);
	}
}