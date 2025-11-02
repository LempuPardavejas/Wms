import React, { useRef, useState } from 'react';
import { Box, Button, Paper, Typography, IconButton, Alert } from '@mui/material';
import CameraAltIcon from '@mui/icons-material/CameraAlt';
import DeleteIcon from '@mui/icons-material/Delete';
import UploadFileIcon from '@mui/icons-material/UploadFile';

interface PhotoCaptureProps {
  onCapture: (photoData: string) => void;
  width?: number;
  height?: number;
}

/**
 * PhotoCapture - Photo upload/capture component
 *
 * Features:
 * - File upload (JPEG, PNG)
 * - Camera capture (if available)
 * - Preview captured photo
 * - Returns base64 image data
 * - Automatic compression for large images
 */
const PhotoCapture: React.FC<PhotoCaptureProps> = ({
  onCapture,
  width = 400,
  height = 300,
}) => {
  const fileInputRef = useRef<HTMLInputElement>(null);
  const videoRef = useRef<HTMLVideoElement>(null);
  const [photoData, setPhotoData] = useState<string>('');
  const [cameraActive, setCameraActive] = useState(false);
  const [error, setError] = useState<string>('');

  const startCamera = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: { width, height, facingMode: 'user' },
      });

      if (videoRef.current) {
        videoRef.current.srcObject = stream;
        videoRef.current.play();
        setCameraActive(true);
        setError('');
      }
    } catch (err) {
      console.error('Camera error:', err);
      setError('Nepavyko paleisti kameros. Naudokite failų įkėlimą.');
    }
  };

  const stopCamera = () => {
    if (videoRef.current && videoRef.current.srcObject) {
      const stream = videoRef.current.srcObject as MediaStream;
      stream.getTracks().forEach(track => track.stop());
      videoRef.current.srcObject = null;
      setCameraActive(false);
    }
  };

  const capturePhoto = () => {
    if (!videoRef.current) return;

    const canvas = document.createElement('canvas');
    canvas.width = width;
    canvas.height = height;

    const ctx = canvas.getContext('2d');
    if (!ctx) return;

    ctx.drawImage(videoRef.current, 0, 0, width, height);

    const imageData = canvas.toDataURL('image/jpeg', 0.8); // Compress to 80% quality
    setPhotoData(imageData);
    onCapture(imageData);

    stopCamera();
  };

  const handleFileUpload = (event: React.ChangeEvent<HTMLInputElement>) => {
    const file = event.target.files?.[0];
    if (!file) return;

    // Validate file type
    if (!file.type.startsWith('image/')) {
      setError('Prašome pasirinkti nuotrauką (JPEG, PNG)');
      return;
    }

    // Validate file size (max 5MB)
    if (file.size > 5 * 1024 * 1024) {
      setError('Nuotrauka per didelė. Maksimalus dydis: 5MB');
      return;
    }

    const reader = new FileReader();
    reader.onload = (e) => {
      const imageData = e.target?.result as string;

      // Compress if needed
      const img = new Image();
      img.onload = () => {
        const canvas = document.createElement('canvas');
        let newWidth = img.width;
        let newHeight = img.height;

        // Resize if too large
        const maxDimension = 800;
        if (newWidth > maxDimension || newHeight > maxDimension) {
          if (newWidth > newHeight) {
            newHeight = (newHeight / newWidth) * maxDimension;
            newWidth = maxDimension;
          } else {
            newWidth = (newWidth / newHeight) * maxDimension;
            newHeight = maxDimension;
          }
        }

        canvas.width = newWidth;
        canvas.height = newHeight;

        const ctx = canvas.getContext('2d');
        if (!ctx) return;

        ctx.drawImage(img, 0, 0, newWidth, newHeight);

        const compressedData = canvas.toDataURL('image/jpeg', 0.8);
        setPhotoData(compressedData);
        onCapture(compressedData);
        setError('');
      };
      img.src = imageData;
    };

    reader.readAsDataURL(file);
  };

  const clearPhoto = () => {
    setPhotoData('');
    onCapture('');
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  return (
    <Box>
      <Typography variant="body2" color="text.secondary" gutterBottom>
        Nuotrauka (optional):
      </Typography>

      {error && (
        <Alert severity="error" onClose={() => setError('')} sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!photoData && !cameraActive && (
        <Box display="flex" gap={1} mb={2}>
          <Button
            variant="outlined"
            startIcon={<CameraAltIcon />}
            onClick={startCamera}
            size="small"
          >
            Fotografuoti
          </Button>
          <Button
            variant="outlined"
            startIcon={<UploadFileIcon />}
            onClick={() => fileInputRef.current?.click()}
            size="small"
          >
            Įkelti failą
          </Button>
        </Box>
      )}

      <input
        ref={fileInputRef}
        type="file"
        accept="image/*"
        onChange={handleFileUpload}
        style={{ display: 'none' }}
      />

      {cameraActive && (
        <Box>
          <Paper elevation={2} sx={{ display: 'inline-block', mb: 1 }}>
            <video
              ref={videoRef}
              width={width}
              height={height}
              style={{ display: 'block' }}
            />
          </Paper>
          <Box display="flex" gap={1}>
            <Button
              variant="contained"
              color="primary"
              startIcon={<CameraAltIcon />}
              onClick={capturePhoto}
              size="small"
            >
              Fotografuoti
            </Button>
            <Button
              variant="outlined"
              onClick={stopCamera}
              size="small"
            >
              Atšaukti
            </Button>
          </Box>
        </Box>
      )}

      {photoData && (
        <Box>
          <Paper elevation={2} sx={{ display: 'inline-block', mb: 1 }}>
            <img
              src={photoData}
              alt="Captured"
              style={{ display: 'block', maxWidth: width, maxHeight: height }}
            />
          </Paper>
          <Box>
            <IconButton
              color="error"
              onClick={clearPhoto}
              size="small"
            >
              <DeleteIcon />
            </IconButton>
            <Typography variant="caption" color="text.secondary" ml={1}>
              Nuotrauka įkelta
            </Typography>
          </Box>
        </Box>
      )}
    </Box>
  );
};

export default PhotoCapture;
