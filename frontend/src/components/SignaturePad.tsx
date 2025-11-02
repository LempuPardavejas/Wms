import React, { useRef, useEffect, useState } from 'react';
import { Box, Button, Paper, Typography } from '@mui/material';
import ClearIcon from '@mui/icons-material/Clear';

interface SignaturePadProps {
  onSave: (signatureData: string) => void;
  width?: number;
  height?: number;
  penColor?: string;
  backgroundColor?: string;
}

/**
 * SignaturePad - Canvas-based signature capture component
 *
 * Features:
 * - Touch and mouse support
 * - Clear button
 * - Returns base64 PNG data
 * - Responsive design
 * - Smooth drawing
 */
const SignaturePad: React.FC<SignaturePadProps> = ({
  onSave,
  width = 600,
  height = 200,
  penColor = '#000000',
  backgroundColor = '#ffffff',
}) => {
  const canvasRef = useRef<HTMLCanvasElement>(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const [isEmpty, setIsEmpty] = useState(true);

  useEffect(() => {
    const canvas = canvasRef.current;
    if (!canvas) return;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    // Set canvas size
    canvas.width = width;
    canvas.height = height;

    // Fill background
    ctx.fillStyle = backgroundColor;
    ctx.fillRect(0, 0, width, height);

    // Set pen properties
    ctx.strokeStyle = penColor;
    ctx.lineWidth = 2;
    ctx.lineCap = 'round';
    ctx.lineJoin = 'round';
  }, [width, height, penColor, backgroundColor]);

  const getCoordinates = (event: React.MouseEvent | React.TouchEvent): { x: number; y: number } => {
    const canvas = canvasRef.current;
    if (!canvas) return { x: 0, y: 0 };

    const rect = canvas.getBoundingClientRect();

    if ('touches' in event) {
      return {
        x: event.touches[0].clientX - rect.left,
        y: event.touches[0].clientY - rect.top,
      };
    } else {
      return {
        x: event.clientX - rect.left,
        y: event.clientY - rect.top,
      };
    }
  };

  const startDrawing = (event: React.MouseEvent | React.TouchEvent) => {
    event.preventDefault();
    setIsDrawing(true);
    setIsEmpty(false);

    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!ctx) return;

    const { x, y } = getCoordinates(event);
    ctx.beginPath();
    ctx.moveTo(x, y);
  };

  const draw = (event: React.MouseEvent | React.TouchEvent) => {
    event.preventDefault();
    if (!isDrawing) return;

    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!ctx) return;

    const { x, y } = getCoordinates(event);
    ctx.lineTo(x, y);
    ctx.stroke();
  };

  const stopDrawing = (event: React.MouseEvent | React.TouchEvent) => {
    event.preventDefault();
    setIsDrawing(false);

    // Save signature
    const canvas = canvasRef.current;
    if (!canvas || isEmpty) return;

    const signatureData = canvas.toDataURL('image/png');
    onSave(signatureData);
  };

  const clearSignature = () => {
    const canvas = canvasRef.current;
    const ctx = canvas?.getContext('2d');
    if (!ctx) return;

    // Clear canvas
    ctx.fillStyle = backgroundColor;
    ctx.fillRect(0, 0, width, height);

    setIsEmpty(true);
    onSave(''); // Clear saved signature
  };

  return (
    <Box>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Pasirašykite čia:
      </Typography>
      <Paper
        elevation={2}
        sx={{
          display: 'inline-block',
          border: '2px dashed',
          borderColor: 'divider',
          cursor: 'crosshair',
          touchAction: 'none', // Prevent scrolling on touch
        }}
      >
        <canvas
          ref={canvasRef}
          onMouseDown={startDrawing}
          onMouseMove={draw}
          onMouseUp={stopDrawing}
          onMouseLeave={stopDrawing}
          onTouchStart={startDrawing}
          onTouchMove={draw}
          onTouchEnd={stopDrawing}
          style={{ display: 'block' }}
        />
      </Paper>
      <Box mt={1}>
        <Button
          variant="outlined"
          size="small"
          startIcon={<ClearIcon />}
          onClick={clearSignature}
          disabled={isEmpty}
        >
          Išvalyti
        </Button>
      </Box>
    </Box>
  );
};

export default SignaturePad;
